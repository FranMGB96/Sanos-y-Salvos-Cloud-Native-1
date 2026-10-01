package com.sanosysalvos.petservice.messaging.audit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Evento "cambio de estado de reporte" tal como lo recibe este servicio (copia propia). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportStatusChangedEvent {

    private Long reportId;
    private String previousStatus;
    private String newStatus;
    private String occurredAt;
}
