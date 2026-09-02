package com.sanosysalvos.bff.dto;

import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DashboardDto {
    private long totalUsuarios;
    private long totalMascotas;
    private long totalReportes;
    private long reportesActivos;
    private long reportesPerdidos;
    private long reportesEncontrados;
    private List<ReporteConDetalleDto> ultimosReportes;
}
