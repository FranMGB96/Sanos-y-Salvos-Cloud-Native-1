package com.sanosysalvos.kafkaadmin.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

/**
 * - AdminClient: el cliente de administración de Kafka que usa la API REST.
 * - NewTopic: al iniciar, crea los topics del proyecto con su configuración por flujo
 *   (si ya existen, no los toca). Es el "dueño" de la definición de los topics.
 */
@Configuration
public class KafkaAdminConfig {

    private final KafkaTopicsProperties props;

    public KafkaAdminConfig(KafkaTopicsProperties props) {
        this.props = props;
    }

    @Bean(destroyMethod = "close")
    public AdminClient adminClient(KafkaAdmin kafkaAdmin) {
        return AdminClient.create(kafkaAdmin.getConfigurationProperties());
    }

    /** CU1: historial de eventos, se guarda 7 días para poder volver a leerlo. */
    @Bean
    public NewTopic topicEventos() {
        return crear(props.getReportEvents(), props.configDe("report-events"));
    }

    /** CU2: alertas por zona, solo sirven 24 horas. */
    @Bean
    public NewTopic topicAlertas() {
        return crear(props.getAlerts(), props.configDe("alerts"));
    }

    /** Dead Letter Topics: mensajes que fallaron, se guardan 14 días para revisarlos. */
    @Bean
    public NewTopic topicEventosDlt() {
        return crear(props.getReportEvents() + props.getDltSuffix(), props.configDe("dead-letter"));
    }

    @Bean
    public NewTopic topicAlertasDlt() {
        return crear(props.getAlerts() + props.getDltSuffix(), props.configDe("dead-letter"));
    }

    private NewTopic crear(String nombre, KafkaTopicsProperties.TopicConfig c) {
        return TopicBuilder.name(nombre)
                .partitions(c.getPartitions())
                .replicas(c.getReplicas())
                .config("retention.ms", String.valueOf(c.getRetentionMs()))
                .config("cleanup.policy", c.getCleanupPolicy())
                .config("min.insync.replicas", String.valueOf(c.getMinInsyncReplicas()))
                .build();
    }
}
