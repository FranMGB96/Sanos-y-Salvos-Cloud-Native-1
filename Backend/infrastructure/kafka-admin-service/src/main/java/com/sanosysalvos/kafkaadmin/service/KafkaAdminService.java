package com.sanosysalvos.kafkaadmin.service;

import com.sanosysalvos.kafkaadmin.dto.ConsumerGroupDetail;
import com.sanosysalvos.kafkaadmin.dto.ConsumerGroupSummary;
import com.sanosysalvos.kafkaadmin.dto.CreateTopicRequest;
import com.sanosysalvos.kafkaadmin.dto.TopicDetail;
import com.sanosysalvos.kafkaadmin.dto.TopicSummary;
import com.sanosysalvos.kafkaadmin.dto.UpdateTopicConfigRequest;
import com.sanosysalvos.kafkaadmin.exception.InvalidRequestException;
import com.sanosysalvos.kafkaadmin.exception.ResourceNotFoundException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AlterConfigOp;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.ListOffsetsResult;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.MemberDescription;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.config.ConfigResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Lógica de administración de Kafka con AdminClient.
 * Las operaciones de AdminClient son asíncronas (KafkaFuture); aquí se espera el resultado
 * con un tiempo máximo y los errores de Kafka se traducen a errores HTTP en el GlobalExceptionHandler.
 */
@Service
public class KafkaAdminService {

    private static final Logger log = LoggerFactory.getLogger(KafkaAdminService.class);
    private static final long TIMEOUT_SEG = 10;

    /** Configuraciones que se muestran en el detalle de un topic. */
    private static final Set<String> CONFIGS_VISIBLES = Set.of(
            "retention.ms", "retention.bytes", "cleanup.policy", "min.insync.replicas", "segment.bytes");

    private final AdminClient admin;

    public KafkaAdminService(AdminClient admin) {
        this.admin = admin;
    }

    // ── Topics ────────────────────────────────────────────────────

    public List<TopicSummary> listarTopics(boolean incluirInternos) {
        Set<String> nombres = esperar(admin.listTopics(new ListTopicsOptions().listInternal(incluirInternos)).names());
        if (nombres.isEmpty()) {
            return List.of();
        }
        Map<String, TopicDescription> descripciones = esperar(admin.describeTopics(nombres).allTopicNames());
        return descripciones.values().stream()
                .map(d -> new TopicSummary(d.name(), d.partitions().size(),
                        d.partitions().isEmpty() ? 0 : d.partitions().get(0).replicas().size(), d.isInternal()))
                .sorted(Comparator.comparing(TopicSummary::name))
                .toList();
    }

    public TopicDetail detalleTopic(String nombre) {
        TopicDescription d = esperar(admin.describeTopics(List.of(nombre)).allTopicNames()).get(nombre);

        // Offsets de inicio y fin de cada partición (cuántos mensajes hay guardados)
        Map<TopicPartition, OffsetSpec> inicio = new HashMap<>();
        Map<TopicPartition, OffsetSpec> fin = new HashMap<>();
        for (TopicPartitionInfo p : d.partitions()) {
            TopicPartition tp = new TopicPartition(nombre, p.partition());
            inicio.put(tp, OffsetSpec.earliest());
            fin.put(tp, OffsetSpec.latest());
        }
        Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> offsetsInicio = esperar(admin.listOffsets(inicio).all());
        Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> offsetsFin = esperar(admin.listOffsets(fin).all());

        List<TopicDetail.PartitionInfo> particiones = d.partitions().stream()
                .map(p -> {
                    TopicPartition tp = new TopicPartition(nombre, p.partition());
                    return new TopicDetail.PartitionInfo(p.partition(),
                            p.leader() == null ? null : p.leader().id(),
                            p.replicas().stream().map(Node::id).toList(),
                            p.isr().stream().map(Node::id).toList(),
                            offsetsInicio.get(tp).offset(),
                            offsetsFin.get(tp).offset());
                })
                .toList();

        int rf = d.partitions().isEmpty() ? 0 : d.partitions().get(0).replicas().size();
        return new TopicDetail(d.name(), d.isInternal(), rf, particiones, configDeTopic(nombre));
    }

    public TopicDetail crearTopic(CreateTopicRequest req) {
        NewTopic topic = new NewTopic(req.getName(), req.getPartitions(), req.getReplicationFactor());
        Map<String, String> configs = new HashMap<>();
        if (req.getRetentionMs() != null) configs.put("retention.ms", String.valueOf(req.getRetentionMs()));
        if (req.getCleanupPolicy() != null) configs.put("cleanup.policy", req.getCleanupPolicy());
        if (req.getMinInsyncReplicas() != null) {
            if (req.getMinInsyncReplicas() > req.getReplicationFactor()) {
                throw new InvalidRequestException("min.insync.replicas no puede ser mayor que el replication factor");
            }
            configs.put("min.insync.replicas", String.valueOf(req.getMinInsyncReplicas()));
        }
        topic.configs(configs);
        esperar(admin.createTopics(List.of(topic)).all());
        log.info("[Kafka Admin] Topic creado: {} ({} particiones, RF {}, configs {})",
                req.getName(), req.getPartitions(), req.getReplicationFactor(), configs);
        return detalleTopic(req.getName());
    }

    public TopicDetail modificarConfig(String nombre, UpdateTopicConfigRequest req) {
        protegerInterno(nombre);
        if (req.estaVacio()) {
            throw new InvalidRequestException("Debe indicar al menos una configuración a modificar");
        }
        List<AlterConfigOp> cambios = new ArrayList<>();
        if (req.getRetentionMs() != null) cambios.add(set("retention.ms", String.valueOf(req.getRetentionMs())));
        if (req.getCleanupPolicy() != null) cambios.add(set("cleanup.policy", req.getCleanupPolicy()));
        if (req.getMinInsyncReplicas() != null) cambios.add(set("min.insync.replicas", String.valueOf(req.getMinInsyncReplicas())));

        ConfigResource recurso = new ConfigResource(ConfigResource.Type.TOPIC, nombre);
        Map<ConfigResource, Collection<AlterConfigOp>> peticion = Map.of(recurso, cambios);
        esperar(admin.incrementalAlterConfigs(peticion).all());
        log.info("[Kafka Admin] Configuración modificada en {}: {}", nombre, cambios);
        return detalleTopic(nombre);
    }

    public TopicDetail aumentarParticiones(String nombre, int total) {
        protegerInterno(nombre);
        esperar(admin.createPartitions(Map.of(nombre, NewPartitions.increaseTo(total))).all());
        log.info("[Kafka Admin] Topic {} ahora tiene {} particiones", nombre, total);
        return detalleTopic(nombre);
    }

    public void eliminarTopic(String nombre) {
        protegerInterno(nombre);
        esperar(admin.deleteTopics(List.of(nombre)).all());
        log.warn("[Kafka Admin] Topic eliminado: {}", nombre);
    }

    private Map<String, String> configDeTopic(String nombre) {
        ConfigResource recurso = new ConfigResource(ConfigResource.Type.TOPIC, nombre);
        Config config = esperar(admin.describeConfigs(List.of(recurso)).all()).get(recurso);
        Map<String, String> visibles = new TreeMap<>();
        for (ConfigEntry entry : config.entries()) {
            if (CONFIGS_VISIBLES.contains(entry.name())) {
                visibles.put(entry.name(), entry.value());
            }
        }
        return visibles;
    }

    private static AlterConfigOp set(String clave, String valor) {
        return new AlterConfigOp(new ConfigEntry(clave, valor), AlterConfigOp.OpType.SET);
    }

    /** Los topics internos de Kafka (empiezan con "__") no se pueden tocar desde la API. */
    private static void protegerInterno(String nombre) {
        if (nombre.startsWith("__")) {
            throw new InvalidRequestException("El topic " + nombre + " es interno de Kafka y no se puede modificar");
        }
    }

    // ── Consumer groups ───────────────────────────────────────────

    public List<ConsumerGroupSummary> listarConsumerGroups() {
        Collection<ConsumerGroupListing> grupos = esperar(admin.listConsumerGroups().all());
        return grupos.stream()
                .map(g -> new ConsumerGroupSummary(g.groupId(),
                        g.state().map(Enum::name).orElse("DESCONOCIDO")))
                .sorted(Comparator.comparing(ConsumerGroupSummary::groupId))
                .toList();
    }

    public ConsumerGroupDetail detalleConsumerGroup(String groupId) {
        ConsumerGroupDescription desc = esperar(admin.describeConsumerGroups(List.of(groupId)).describedGroups().get(groupId));
        if ("DEAD".equals(desc.state().name()) && desc.members().isEmpty()) {
            // Kafka responde DEAD para un grupo que no existe
            Map<TopicPartition, OffsetAndMetadata> sinOffsets =
                    esperar(admin.listConsumerGroupOffsets(groupId).partitionsToOffsetAndMetadata());
            if (sinOffsets.isEmpty()) {
                throw new ResourceNotFoundException("Consumer group no encontrado: " + groupId);
            }
        }

        // Qué consumidor tiene cada partición
        Map<TopicPartition, String> duenioDeParticion = new HashMap<>();
        List<ConsumerGroupDetail.MemberInfo> miembros = new ArrayList<>();
        for (MemberDescription m : desc.members()) {
            List<String> asignadas = m.assignment().topicPartitions().stream()
                    .sorted(Comparator.comparing(TopicPartition::topic).thenComparingInt(TopicPartition::partition))
                    .map(tp -> tp.topic() + "-" + tp.partition())
                    .toList();
            m.assignment().topicPartitions().forEach(tp -> duenioDeParticion.put(tp, m.consumerId()));
            miembros.add(new ConsumerGroupDetail.MemberInfo(m.consumerId(), m.clientId(), m.host(), asignadas));
        }

        // Offsets confirmados por el grupo vs. último offset de cada partición = lag
        Map<TopicPartition, OffsetAndMetadata> confirmados =
                esperar(admin.listConsumerGroupOffsets(groupId).partitionsToOffsetAndMetadata());
        Set<TopicPartition> particiones = new HashSet<>(confirmados.keySet());
        particiones.addAll(duenioDeParticion.keySet());

        Map<TopicPartition, OffsetSpec> pedirFin = particiones.stream()
                .collect(Collectors.toMap(tp -> tp, tp -> OffsetSpec.latest()));
        Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> finales =
                pedirFin.isEmpty() ? Map.of() : esperar(admin.listOffsets(pedirFin).all());

        List<ConsumerGroupDetail.PartitionLag> lags = particiones.stream()
                .sorted(Comparator.comparing(TopicPartition::topic).thenComparingInt(TopicPartition::partition))
                .map(tp -> {
                    OffsetAndMetadata om = confirmados.get(tp);
                    Long confirmado = om == null ? null : om.offset();
                    long fin = finales.containsKey(tp) ? finales.get(tp).offset() : 0L;
                    long lag = confirmado == null ? fin : Math.max(0, fin - confirmado);
                    return new ConsumerGroupDetail.PartitionLag(tp.topic(), tp.partition(), confirmado, fin, lag,
                            duenioDeParticion.get(tp));
                })
                .toList();

        long totalLag = lags.stream().mapToLong(ConsumerGroupDetail.PartitionLag::lag).sum();
        Node coordinador = desc.coordinator();
        return new ConsumerGroupDetail(desc.groupId(), desc.state().name(),
                coordinador == null ? null : "broker " + coordinador.id() + " (" + coordinador.host() + ")",
                desc.partitionAssignor(), desc.members().size(), totalLag, miembros, lags);
    }

    // ── Clúster ───────────────────────────────────────────────────

    public Map<String, Object> infoCluster() {
        var cluster = admin.describeCluster();
        Collection<Node> nodos = esperar(cluster.nodes());
        Node controlador = esperar(cluster.controller());
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("clusterId", esperar(cluster.clusterId()));
        info.put("controlador", controlador == null ? null : controlador.id());
        info.put("brokers", nodos.stream()
                .sorted(Comparator.comparingInt(Node::id))
                .map(n -> Map.of("id", n.id(), "host", n.host(), "puerto", n.port()))
                .toList());
        return info;
    }

    // ── Utilidad ──────────────────────────────────────────────────

    /** Espera el resultado de AdminClient y deja pasar la excepción real de Kafka. */
    private static <T> T esperar(KafkaFuture<T> future) {
        try {
            return future.get(TIMEOUT_SEG, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            Throwable causa = e.getCause();
            if (causa instanceof RuntimeException re) {
                throw re;
            }
            throw new IllegalStateException(causa);
        } catch (TimeoutException e) {
            throw new org.apache.kafka.common.errors.TimeoutException(
                    "Kafka no respondió en " + TIMEOUT_SEG + " segundos");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Operación interrumpida", e);
        }
    }
}
