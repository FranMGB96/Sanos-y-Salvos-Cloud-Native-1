package com.sanosysalvos.userservice.messaging.kafka;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Evento del topic sys.reports.alerts (lo genera analytics-service, CU2).
 * Copia propia del DTO, con validaciones para revisar el mensaje antes de procesarlo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaEvent {

    /** Id único de la alerta: evita procesarla dos veces. */
    @NotBlank
    private String alertaId;

    /** Zona afectada (latitud_longitud redondeadas a 2 decimales). También es la key del mensaje. */
    @NotBlank
    private String zona;

    @NotBlank
    private String tipo;

    @NotNull
    @Min(1)
    private Integer cantidad;

    private Integer ventanaMinutos;

    /** Reportes que generaron la alerta (trazabilidad). */
    private List<Long> reportIds;

    @NotBlank
    @Pattern(regexp = "MEDIA|ALTA", message = "severidad debe ser MEDIA o ALTA")
    private String severidad;

    @NotBlank
    private String motivo;

    @NotBlank
    private String timestamp;
}
