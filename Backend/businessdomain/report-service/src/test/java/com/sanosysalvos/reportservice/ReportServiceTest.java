package com.sanosysalvos.reportservice;

import com.sanosysalvos.reportservice.dto.ReportDto;
import com.sanosysalvos.reportservice.exception.ResourceNotFoundException;
import com.sanosysalvos.reportservice.model.Report;
import com.sanosysalvos.reportservice.repository.ReportRepository;
import com.sanosysalvos.reportservice.service.ReportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {
    @Mock private ReportRepository reportRepository;
    @InjectMocks private ReportService reportService;

    @Test void createReport_datosValidos_retornaReporte() {
        ReportDto dto = ReportDto.builder().tipo("PERDIDO").descripcion("Se perdió en el parque").reporterUserId(1L).build();
        Report saved = Report.builder().id(1L).tipo(Report.TipoReporte.PERDIDO).descripcion("Se perdió en el parque").reporterUserId(1L).estado(Report.EstadoReporte.ACTIVO).build();
        when(reportRepository.save(any())).thenReturn(saved);
        ReportDto result = reportService.createReport(dto);
        assertNotNull(result); assertEquals("PERDIDO", result.getTipo()); assertEquals("ACTIVO", result.getEstado());
    }

    @Test void getReportById_noExiste_lanzaExcepcion() {
        when(reportRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> reportService.getReportById(99L));
    }

    @Test void updateEstado_cambiaCorrecto() {
        Report r = Report.builder().id(1L).tipo(Report.TipoReporte.PERDIDO).descripcion("test").reporterUserId(1L).estado(Report.EstadoReporte.ACTIVO).build();
        when(reportRepository.findById(1L)).thenReturn(Optional.of(r));
        when(reportRepository.save(any())).thenReturn(r);
        ReportDto result = reportService.updateEstado(1L, "RESUELTO", 1L, "OWNER");
    }

    @Test void getByTipo_retornaFiltrados() {
        Report r = Report.builder().id(1L).tipo(Report.TipoReporte.ENCONTRADO).descripcion("test").reporterUserId(1L).estado(Report.EstadoReporte.ACTIVO).build();
        when(reportRepository.findByTipo(Report.TipoReporte.ENCONTRADO)).thenReturn(List.of(r));
        List<ReportDto> result = reportService.getByTipo("ENCONTRADO");
        assertEquals(1, result.size()); assertEquals("ENCONTRADO", result.get(0).getTipo());
    }
}
