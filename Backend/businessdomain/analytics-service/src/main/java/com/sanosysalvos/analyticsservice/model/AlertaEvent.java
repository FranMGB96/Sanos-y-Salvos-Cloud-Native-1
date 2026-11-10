package com.sanosysalvos.analyticsservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Evento de salida del topic sys.reports.alerts (CU2). La key del mensaje es la zona. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaEvent {
    private String alertaId;
    private String zona;
    private String tipo;
    private Integer cantidad;
    private Integer ventanaMinutos;
    private List<Long> reportIds;
    private String severidad;
    private String motivo;
    private String timestamp;
}
