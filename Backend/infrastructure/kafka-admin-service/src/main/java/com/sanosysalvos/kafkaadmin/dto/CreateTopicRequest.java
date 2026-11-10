package com.sanosysalvos.kafkaadmin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "Datos para crear un topic")
public class CreateTopicRequest {

    @NotBlank
    @Pattern(regexp = ValidationPatterns.TOPIC_NAME, message = "nombre de topic inválido")
    @Schema(example = "sys.reports.prueba")
    private String name;

    @NotNull
    @Min(value = 1, message = "debe tener al menos 1 partición")
    @Max(value = 50, message = "máximo 50 particiones")
    @Schema(example = "3")
    private Integer partitions;

    @NotNull
    @Min(value = 1, message = "replication factor mínimo 1")
    @Max(value = 3, message = "replication factor máximo 3 (hay 3 brokers)")
    @Schema(example = "3")
    private Short replicationFactor;

    @Min(value = 60000, message = "la retención mínima es 1 minuto (60000 ms)")
    @Schema(example = "604800000", description = "Retención en ms (opcional)")
    private Long retentionMs;

    @Pattern(regexp = ValidationPatterns.CLEANUP_POLICY, message = "cleanup.policy debe ser delete, compact o compact,delete")
    @Schema(example = "delete")
    private String cleanupPolicy;

    @Min(1)
    @Max(3)
    @Schema(example = "2")
    private Integer minInsyncReplicas;
}
