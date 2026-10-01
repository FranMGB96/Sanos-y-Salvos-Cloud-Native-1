package com.sanosysalvos.userservice.messaging.notification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Evento "reporte creado" tal como lo recibe este servicio (copia propia: servicios desacoplados). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportCreatedEvent {

    private Long reportId;
    private String tipo;
    private String descripcion;
    private Long reporterUserId;
    private Long petId;
    private String occurredAt;
}
