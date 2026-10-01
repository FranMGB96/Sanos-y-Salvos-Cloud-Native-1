package com.sanosysalvos.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Cuerpo de POST /messages (publicar un mensaje de prueba, útil para la demo de DLQ). */
@Data
@NoArgsConstructor
public class PublishRequest {

    @NotBlank(message = "El exchange es obligatorio")
    @Pattern(regexp = ValidationPatterns.RESOURCE_NAME, message = "Nombre de exchange inválido (letras, números, '.', '_' y '-')")
    private String exchange;

    @NotNull(message = "routingKey es obligatoria")
    @Size(max = 255, message = "routingKey admite máximo 255 caracteres")
    private String routingKey;

    /** Texto que se envía tal cual (normalmente un JSON). */
    @NotBlank(message = "El payload es obligatorio")
    @Size(max = 10000, message = "El payload admite máximo 10000 caracteres")
    private String payload;
}
