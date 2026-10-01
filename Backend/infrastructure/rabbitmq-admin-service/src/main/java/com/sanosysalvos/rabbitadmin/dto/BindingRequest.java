package com.sanosysalvos.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Cuerpo de POST /bindings y DELETE /bindings */
@Data
@NoArgsConstructor
public class BindingRequest {

    @NotBlank(message = "El exchange de origen es obligatorio")
    @Pattern(regexp = ValidationPatterns.RESOURCE_NAME, message = "Nombre de exchange inválido (letras, números, '.', '_' y '-')")
    private String exchange;

    @NotBlank(message = "El destino (cola o exchange) es obligatorio")
    @Pattern(regexp = ValidationPatterns.RESOURCE_NAME, message = "Nombre de destino inválido (letras, números, '.', '_' y '-')")
    private String destination;

    @NotBlank(message = "destinationType es obligatorio")
    @Pattern(regexp = "^(QUEUE|EXCHANGE)$", message = "destinationType debe ser QUEUE o EXCHANGE")
    private String destinationType;

    /** Puede ir vacía (por ejemplo en fanout), pero no nula. */
    @NotNull(message = "routingKey es obligatoria (use \"\" si el exchange es fanout)")
    @Size(max = 255, message = "routingKey admite máximo 255 caracteres")
    private String routingKey;
}
