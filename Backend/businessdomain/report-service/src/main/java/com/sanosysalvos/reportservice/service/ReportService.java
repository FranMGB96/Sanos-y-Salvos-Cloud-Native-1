package com.sanosysalvos.reportservice.service;

import com.sanosysalvos.reportservice.dto.ReportDto;
import com.sanosysalvos.reportservice.exception.ResourceNotFoundException;
import com.sanosysalvos.reportservice.exception.UnauthorizedException;
import com.sanosysalvos.reportservice.model.Report;
import com.sanosysalvos.reportservice.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired private ReportRepository reportRepository;

    public List<ReportDto> getAllReports() {
        return reportRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public ReportDto getReportById(Long id) { return toDto(findOrThrow(id)); }

    public List<ReportDto> getByTipo(String tipo) {
        return reportRepository.findByTipo(Report.TipoReporte.valueOf(tipo.toUpperCase()))
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<ReportDto> getByEstado(String estado) {
        return reportRepository.findByEstado(Report.EstadoReporte.valueOf(estado.toUpperCase()))
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<ReportDto> getByUsuario(Long userId) {
        return reportRepository.findByReporterUserId(userId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<ReportDto> getByPet(Long petId) {
        return reportRepository.findByPetId(petId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public ReportDto createReport(ReportDto dto) {
        Report r = Report.builder()
                .tipo(Report.TipoReporte.valueOf(dto.getTipo().toUpperCase()))
                .descripcion(dto.getDescripcion())
                .latitud(dto.getLatitud())
                .longitud(dto.getLongitud())
                .ubicacionDescripcion(dto.getUbicacionDescripcion())
                .petId(dto.getPetId())
                .reporterUserId(dto.getReporterUserId())
                .build();
        return toDto(reportRepository.save(r));
    }

    public ReportDto updateReport(Long id, ReportDto dto, Long requestingUserId, String requestingUserRole) {
        Report r = findOrThrow(id);

        boolean esAdmin = "ADMIN".equals(requestingUserRole);
        boolean esDuenio = r.getReporterUserId().equals(requestingUserId);

        if (!esAdmin && !esDuenio) {
            throw new UnauthorizedException("No tienes permiso para modificar este reporte");
        }

        if (dto.getDescripcion()          != null) r.setDescripcion(dto.getDescripcion());
        if (dto.getLatitud()              != null) r.setLatitud(dto.getLatitud());
        if (dto.getLongitud()             != null) r.setLongitud(dto.getLongitud());
        if (dto.getUbicacionDescripcion() != null) r.setUbicacionDescripcion(dto.getUbicacionDescripcion());
        if (dto.getEstado()               != null) r.setEstado(Report.EstadoReporte.valueOf(dto.getEstado().toUpperCase()));
        r.setUpdatedAt(LocalDateTime.now());

        return toDto(reportRepository.save(r));
    }

    public ReportDto updateEstado(Long id, String nuevoEstado, Long requestingUserId, String requestingUserRole) {
        Report r = findOrThrow(id);

        boolean esAdmin = "ADMIN".equals(requestingUserRole);
        boolean esDuenio = r.getReporterUserId().equals(requestingUserId);

        if (!esAdmin && !esDuenio) {
            throw new UnauthorizedException("No tienes permiso para modificar este reporte");
        }

        r.setEstado(Report.EstadoReporte.valueOf(nuevoEstado.toUpperCase()));
        r.setUpdatedAt(LocalDateTime.now());
        return toDto(reportRepository.save(r));
    }

    public void deleteReport(Long id, Long requestingUserId, String requestingUserRole) {
        Report r = findOrThrow(id);

        boolean esAdmin = "ADMIN".equals(requestingUserRole);
        boolean esDuenio = r.getReporterUserId().equals(requestingUserId);

        if (!esAdmin && !esDuenio) {
            throw new UnauthorizedException("No tienes permiso para eliminar este reporte");
        }

        r.setEstado(Report.EstadoReporte.CERRADO);
        r.setUpdatedAt(LocalDateTime.now());
        reportRepository.save(r);
    }
    private Report findOrThrow(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado: " + id));
    }

    private ReportDto toDto(Report r) {
        return ReportDto.builder()
                .id(r.getId()).tipo(r.getTipo().name()).descripcion(r.getDescripcion())
                .latitud(r.getLatitud()).longitud(r.getLongitud())
                .ubicacionDescripcion(r.getUbicacionDescripcion())
                .petId(r.getPetId()).reporterUserId(r.getReporterUserId())
                .estado(r.getEstado().name()).createdAt(r.getCreatedAt()).updatedAt(r.getUpdatedAt())
                .build();
    }
}