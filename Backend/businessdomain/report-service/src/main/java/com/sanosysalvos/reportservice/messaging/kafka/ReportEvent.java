package com.sanosysalvos.reportservice.messaging.kafka;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Evento que viaja como JSON por el topic sys.reports.events (Kafka).
 *
 * Se publica cada vez que se crea un reporte o cambia su estado.
 * La key del mensaje es el reportId, así todos los eventos de un mismo
 * reporte caen en la misma partición y se leen en orden.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportEvent {

    public static final String REPORTE_CREADO = "REPORTE_CREADO";
    public static final String ESTADO_CAMBIADO = "ESTADO_CAMBIADO";

    /** Id único del evento: el consumidor lo usa para no procesar dos veces lo mismo. */
    @NotBlank
    private String eventId;

    /** REPORTE_CREADO o ESTADO_CAMBIADO: le dice al consumidor qué pasó. */
    @NotBlank
    @Pattern(regexp = "REPORTE_CREADO|ESTADO_CAMBIADO")
    private String tipoEvento;

    @NotNull
    private Long reportId;

    /** Puede venir null si el reporte no tiene una mascota registrada. */
    private Long petId;

    /** PERDIDO o ENCONTRADO. */
    @NotBlank
    private String tipo;

    /** Estado antes del cambio (null cuando el reporte es nuevo). */
    private String estadoAnterior;

    @NotBlank
    private String estado;

    /** Con latitud y longitud se calcula la zona para las alertas. */
    private Double latitud;
    private Double longitud;

    private Long reporterUserId;

    /** Cuándo ocurrió el hecho (ISO-8601, UTC). */
    @NotBlank
    private String timestamp;
}
