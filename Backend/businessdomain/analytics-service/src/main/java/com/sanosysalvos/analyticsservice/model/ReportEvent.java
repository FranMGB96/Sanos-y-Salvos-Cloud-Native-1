package com.sanosysalvos.analyticsservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Evento del topic sys.reports.events (copia propia del DTO que publica report-service). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportEvent {

    public static final String REPORTE_CREADO = "REPORTE_CREADO";
    public static final String ESTADO_CAMBIADO = "ESTADO_CAMBIADO";

    private String eventId;
    private String tipoEvento;
    private Long reportId;
    private Long petId;
    private String tipo;
    private String estadoAnterior;
    private String estado;
    private Double latitud;
    private Double longitud;
    private Long reporterUserId;
    private String timestamp;

    /** Lo que cuenta para las alertas: un reporte NUEVO de mascota PERDIDA con ubicación. */
    public boolean esPerdidoNuevoConUbicacion() {
        return REPORTE_CREADO.equals(tipoEvento) && "PERDIDO".equals(tipo)
                && latitud != null && longitud != null;
    }
}
