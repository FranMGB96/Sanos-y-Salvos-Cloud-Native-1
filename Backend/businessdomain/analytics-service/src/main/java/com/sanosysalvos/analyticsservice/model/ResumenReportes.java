package com.sanosysalvos.analyticsservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contadores en vivo (CU1). Es el estado que guarda Kafka Streams en su state store,
 * respaldado en un topic changelog: si el servicio se reinicia, los contadores se recuperan solos.
 *
 * Ojo: cuenta los eventos desde que se activó Kafka, no los reportes que ya existían antes.
 */
@Data
@NoArgsConstructor
public class ResumenReportes {

    private long totalReportes;
    private long perdidos;
    private long encontrados;
    private long activos;
    private long resueltos;
    private long cerrados;
    private long eventosProcesados;
    private String ultimaActualizacion;

    /** Aplica un evento a los contadores y devuelve el mismo objeto (para usarlo en aggregate). */
    public ResumenReportes aplicar(ReportEvent e) {
        if (ReportEvent.REPORTE_CREADO.equals(e.getTipoEvento())) {
            totalReportes++;
            if ("PERDIDO".equals(e.getTipo())) perdidos++;
            if ("ENCONTRADO".equals(e.getTipo())) encontrados++;
            sumarEstado(e.getEstado(), 1);
        } else if (ReportEvent.ESTADO_CAMBIADO.equals(e.getTipoEvento())) {
            // Gracias a estadoAnterior no hay que consultar la base de datos para mover los contadores
            sumarEstado(e.getEstadoAnterior(), -1);
            sumarEstado(e.getEstado(), 1);
        }
        eventosProcesados++;
        ultimaActualizacion = e.getTimestamp();
        return this;
    }

    private void sumarEstado(String estado, int delta) {
        if (estado == null) return;
        switch (estado) {
            // Math.max: un reporte antiguo (de antes de Kafka) que cambia de estado no deja contadores negativos
            case "ACTIVO" -> activos = Math.max(0, activos + delta);
            case "RESUELTO" -> resueltos = Math.max(0, resueltos + delta);
            case "CERRADO" -> cerrados = Math.max(0, cerrados + delta);
            default -> { }
        }
    }
}
