package com.sanosysalvos.petservice.messaging.kafka;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Evento del topic sys.reports.events (lo publica report-service).
 * Cada servicio tiene su propia copia del DTO, igual que con RabbitMQ:
 * así ningún servicio depende de las clases de otro.
 * Las anotaciones validan el mensaje antes de procesarlo (@Valid en el listener).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportEvent {

    @NotBlank
    private String eventId;

    @NotBlank
    @Pattern(regexp = "REPORTE_CREADO|ESTADO_CAMBIADO", message = "tipoEvento desconocido")
    private String tipoEvento;

    @NotNull
    private Long reportId;

    private Long petId;

    @NotBlank
    @Pattern(regexp = "PERDIDO|ENCONTRADO", message = "tipo debe ser PERDIDO o ENCONTRADO")
    private String tipo;

    private String estadoAnterior;

    @NotBlank
    private String estado;

    private Double latitud;
    private Double longitud;
    private Long reporterUserId;

    @NotBlank
    private String timestamp;
}
