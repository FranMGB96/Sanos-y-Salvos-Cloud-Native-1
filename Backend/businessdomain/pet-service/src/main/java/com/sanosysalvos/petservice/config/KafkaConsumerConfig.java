package com.sanosysalvos.petservice.config;

import com.sanosysalvos.petservice.messaging.support.InvalidMessageException;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.DelegatingByTypeSerializer;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejo de errores de los consumidores Kafka (guía 3.3.3):
 *
 * 1. Validación: KafkaEventParser convierte el texto a DTO y lo valida (Bean Validation / JSR-380).
 * 2. Reintentos: errores temporales se reintentan con backoff exponencial (1 s, 2 s).
 * 3. Dead Letter Topic: si se agotan los reintentos, el mensaje se publica en <topic> + sufijo DLT
 *    con headers que explican el error (Spring los agrega solo).
 * 4. Mensajes inválidos (InvalidMessageException) no se reintentan: van directo al DLT,
 *    porque reintentar no los arregla. Como el listener recibe texto, el DLT guarda
 *    el mensaje original tal como llegó.
 */
@Configuration
public class KafkaConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    /**
     * Template solo para publicar en el DLT. El valor puede venir como texto (lo normal),
     * como bytes (si falló la lectura del mensaje) o como objeto, así que se elige el
     * serializer según el tipo. El texto se guarda tal cual, sin comillas extra.
     */
    @Bean
    public KafkaTemplate<String, Object> deadLetterKafkaTemplate(
            @Value("${spring.kafka.bootstrap-servers:localhost:9092}") String bootstrapServers) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 3000);

        Map<Class<?>, Serializer<?>> delegates = new LinkedHashMap<>();
        delegates.put(byte[].class, new ByteArraySerializer());
        delegates.put(String.class, new StringSerializer());
        delegates.put(Object.class, new JsonSerializer<>().noTypeInfo());

        DefaultKafkaProducerFactory<String, Object> factory = new DefaultKafkaProducerFactory<>(props,
                new StringSerializer(), new DelegatingByTypeSerializer(delegates, true));
        return new KafkaTemplate<>(factory);
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            KafkaTemplate<String, Object> deadLetterKafkaTemplate,
            @Value("${sanosysalvos.kafka.topics.dlt-suffix}") String dltSuffix,
            @Value("${sanosysalvos.kafka.retry.max-retries:2}") int maxRetries,
            @Value("${sanosysalvos.kafka.retry.initial-interval-ms:1000}") long initialInterval,
            @Value("${sanosysalvos.kafka.retry.multiplier:2.0}") double multiplier) {

        // El DLT se llama igual que el topic original + el sufijo del application.yml (.DLT) y se respeta la partición
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(deadLetterKafkaTemplate,
                (record, ex) -> {
                    log.error("[Kafka] Fallo definitivo en {}-{} offset {}: se envía a {}{}. Causa: {}",
                            record.topic(), record.partition(), record.offset(), record.topic(), dltSuffix, causa(ex));
                    return new TopicPartition(record.topic() + dltSuffix, record.partition());
                });

        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(maxRetries);
        backOff.setInitialInterval(initialInterval);
        backOff.setMultiplier(multiplier);
        backOff.setMaxInterval(10_000L);

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);
        // Estos errores no se arreglan reintentando: directo al DLT
        handler.addNotRetryableExceptions(InvalidMessageException.class);
        handler.setRetryListeners((record, ex, attempt) -> {
            if (esPermanente(ex)) {
                return; // no se reintenta: lo informa el log de "Fallo definitivo"
            }
            log.warn("[Kafka] Falló el intento {} de {} al procesar {}-{} offset {}. Se reintenta. Causa: {}",
                    attempt, maxRetries + 1, record.topic(), record.partition(), record.offset(), causa(ex));
        });
        return handler;
    }

    /** El DLT tiene que existir antes de publicar en él (la creación automática de topics está apagada). */
    @Bean
    public NewTopic deadLetterTopic(
            @Value("${sanosysalvos.kafka.topics.report-events}") String sourceTopic,
            @Value("${sanosysalvos.kafka.topics.dlt-suffix}") String dltSuffix,
            @Value("${sanosysalvos.kafka.topics.config.dead-letter.partitions:3}") int partitions,
            @Value("${sanosysalvos.kafka.topics.config.dead-letter.replicas:3}") int replicas,
            @Value("${sanosysalvos.kafka.topics.config.dead-letter.retention-ms:1209600000}") String retentionMs,
            @Value("${sanosysalvos.kafka.topics.config.dead-letter.min-insync-replicas:2}") String minIsr) {
        return TopicBuilder.name(sourceTopic + dltSuffix)
                .partitions(partitions)
                .replicas(replicas)
                .config("retention.ms", retentionMs)
                .config("cleanup.policy", "delete")
                .config("min.insync.replicas", minIsr)
                .build();
    }

    private static boolean esPermanente(Exception ex) {
        Throwable t = ex;
        while (t != null) {
            if (t instanceof InvalidMessageException || t instanceof DeserializationException) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

    /** Causa en una línea corta: el mensaje del error de más adentro (el que importa). */
    private static String causa(Exception ex) {
        Throwable t = ex;
        while (t.getCause() != null && t.getCause() != t) {
            if (t instanceof InvalidMessageException) {
                break;
            }
            t = t.getCause();
        }
        String mensaje = t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage();
        return mensaje.length() > 150 ? mensaje.substring(0, 150) + "..." : mensaje;
    }
}
