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
public class ReportStatusChangedEvent {

    private Long reportId;
    private String previousStatus;
    private String newStatus;
    private String occurredAt;
}
