package com.sanosysalvos.kafkaadmin.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Map;

/**
 * Nombres y configuración de los topics del proyecto (sanosysalvos.kafka.topics.* en el config-server).
 * Los nombres no tienen valor por defecto en el código: solo existen en el application.yml.
 */
@Data
@Validated
@ConfigurationProperties(prefix = "sanosysalvos.kafka.topics")
public class KafkaTopicsProperties {

    @NotBlank
    private String reportEvents;
    @NotBlank
    private String alerts;
    /** Sufijo de los Dead Letter Topics (.DLT). */
    @NotBlank
    private String dltSuffix;
    private Map<String, TopicConfig> config = new HashMap<>();

    public TopicConfig configDe(String flujo) {
        return config.getOrDefault(flujo, new TopicConfig());
    }

    @Data
    public static class TopicConfig {
        private int partitions = 3;
        private int replicas = 3;
        private long retentionMs = 604_800_000L;
        private String cleanupPolicy = "delete";
        private int minInsyncReplicas = 2;
    }
}
