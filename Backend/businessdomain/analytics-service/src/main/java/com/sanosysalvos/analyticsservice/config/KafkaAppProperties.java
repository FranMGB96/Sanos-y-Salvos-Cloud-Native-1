package com.sanosysalvos.analyticsservice.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Map;

/**
 * Propiedades sanosysalvos.kafka.* (vienen del config-server, archivo application.yml compartido).
 * Los nombres de los topics NO tienen valor por defecto en el código: solo existen en el application.yml.
 * Si faltan, el servicio no arranca y avisa cuál falta (@Validated + @NotBlank).
 */
@Data
@Validated
@ConfigurationProperties(prefix = "sanosysalvos.kafka")
public class KafkaAppProperties {

    @Valid
    private Topics topics = new Topics();
    private Alertas alertas = new Alertas();

    @Data
    public static class Topics {
        @NotBlank
        private String reportEvents;
        @NotBlank
        private String alerts;
        /** Configuración por flujo: report-events, alerts, dead-letter. */
        private Map<String, TopicConfig> config = new HashMap<>();

        public TopicConfig configDe(String flujo) {
            return config.getOrDefault(flujo, new TopicConfig());
        }
    }

    @Data
    public static class TopicConfig {
        private int partitions = 3;
        private int replicas = 3;
        private long retentionMs = 604_800_000L;
        private String cleanupPolicy = "delete";
        private int minInsyncReplicas = 2;
    }

    @Data
    public static class Alertas {
        private int umbralMedia = 3;
        private int umbralAlta = 5;
        private int ventanaMinutos = 60;
    }
}
