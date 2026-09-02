package com.sanosysalvos.bff.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ReporteConDetalleDto {
    private Long id;
    private String tipo;
    private String descripcion;
    private Double latitud;
    private Double longitud;
    private String ubicacionDescripcion;
    private String estado;
    private Long reporterUserId;
    private LocalDateTime createdAt;
    private MascotaResumenDto mascota;
    private String nombreReporter;
    private String telefonoReporter;
}