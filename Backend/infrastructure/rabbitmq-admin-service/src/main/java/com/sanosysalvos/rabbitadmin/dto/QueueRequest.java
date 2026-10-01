package com.sanosysalvos.rabbitadmin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Cuerpo de POST /queues */
@Data
@NoArgsConstructor
public class QueueRequest {

    @NotBlank(message = "El nombre de la cola es obligatorio")
    @Pattern(regexp = ValidationPatterns.NEW_RESOURCE_NAME, message = ValidationPatterns.NAME_MESSAGE)
    private String name;

    /** Por defecto durable (sobrevive a reinicios de RabbitMQ). */
    private Boolean durable = true;

    private Boolean autoDelete = false;

    /** Opcional: exchange al que se envían los mensajes rechazados (DLX). */
    @Size(max = 255, message = "deadLetterExchange admite máximo 255 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9._-]*$", message = "deadLetterExchange solo admite letras, números, '.', '_' y '-'")
    private String deadLetterExchange;

    /** Opcional: routing key con la que se reenvía a la DLX (requiere deadLetterExchange). */
    @Size(max = 255, message = "deadLetterRoutingKey admite máximo 255 caracteres")
    private String deadLetterRoutingKey;

    /** Opcional: tiempo de vida de los mensajes en la cola, en milisegundos. */
    @Min(value = 1, message = "ttlMs debe ser mayor que 0")
    private Integer ttlMs;
}
