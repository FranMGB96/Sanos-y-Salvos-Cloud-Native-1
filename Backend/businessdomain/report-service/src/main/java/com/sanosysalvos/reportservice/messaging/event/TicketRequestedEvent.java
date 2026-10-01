package com.sanosysalvos.reportservice.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Evento que viaja como JSON por RabbitMQ. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketRequestedEvent {

    private Long reportId;
    private String tipo;
    private String descripcion;
    private Long reporterUserId;
    private String occurredAt;
}
