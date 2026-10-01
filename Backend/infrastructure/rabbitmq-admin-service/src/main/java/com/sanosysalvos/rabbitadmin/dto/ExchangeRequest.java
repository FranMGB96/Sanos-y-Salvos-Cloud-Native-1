package com.sanosysalvos.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Cuerpo de POST /exchanges */
@Data
@NoArgsConstructor
public class ExchangeRequest {

    @NotBlank(message = "El nombre del exchange es obligatorio")
    @Pattern(regexp = ValidationPatterns.NEW_RESOURCE_NAME, message = ValidationPatterns.NAME_MESSAGE)
    private String name;

    @NotBlank(message = "El tipo de exchange es obligatorio")
    @Pattern(regexp = "^(direct|topic|fanout|headers)$",
            message = "Tipo inválido: debe ser direct, topic, fanout o headers (en minúsculas)")
    private String type;

    private Boolean durable = true;

    private Boolean autoDelete = false;
}
