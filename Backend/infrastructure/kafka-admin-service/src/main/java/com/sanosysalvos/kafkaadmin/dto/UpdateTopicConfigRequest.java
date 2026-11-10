package com.sanosysalvos.kafkaadmin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** Cambios de configuración de un topic. Todos son opcionales, pero debe venir al menos uno. */
@Data
@Schema(description = "Configuraciones a modificar (solo se cambian las que vienen)")
public class UpdateTopicConfigRequest {

    @Min(value = 60000, message = "la retención mínima es 1 minuto (60000 ms)")
    @Schema(example = "86400000")
    private Long retentionMs;

    @Pattern(regexp = ValidationPatterns.CLEANUP_POLICY, message = "cleanup.policy debe ser delete, compact o compact,delete")
    @Schema(example = "delete")
    private String cleanupPolicy;

    @Min(1)
    @Max(3)
    @Schema(example = "2")
    private Integer minInsyncReplicas;

    public boolean estaVacio() {
        return retentionMs == null && cleanupPolicy == null && minInsyncReplicas == null;
    }
}
