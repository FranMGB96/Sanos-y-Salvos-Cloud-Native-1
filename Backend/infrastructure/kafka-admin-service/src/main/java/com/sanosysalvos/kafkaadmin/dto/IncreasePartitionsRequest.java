package com.sanosysalvos.kafkaadmin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Kafka solo permite AUMENTAR particiones, nunca disminuirlas. */
@Data
public class IncreasePartitionsRequest {

    @NotNull
    @Min(1)
    @Max(50)
    @Schema(example = "6", description = "Total de particiones que tendrá el topic")
    private Integer totalPartitions;
}
