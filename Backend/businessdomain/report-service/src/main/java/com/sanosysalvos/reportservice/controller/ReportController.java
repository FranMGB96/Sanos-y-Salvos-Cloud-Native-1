package com.sanosysalvos.reportservice.controller;

import com.sanosysalvos.reportservice.dto.ReportDto;
import com.sanosysalvos.reportservice.service.ReportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/reports")
@Tag(name = "Reportes")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping
    public ResponseEntity<List<ReportDto>> getAll() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.getReportById(id));
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<ReportDto>> getByTipo(@PathVariable String tipo) {
        return ResponseEntity.ok(reportService.getByTipo(tipo));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<ReportDto>> getByEstado(@PathVariable String estado) {
        return ResponseEntity.ok(reportService.getByEstado(estado));
    }

    @GetMapping("/usuario/{userId}")
    public ResponseEntity<List<ReportDto>> getByUsuario(@PathVariable Long userId) {
        return ResponseEntity.ok(reportService.getByUsuario(userId));
    }

    @GetMapping("/mascota/{petId}")
    public ResponseEntity<List<ReportDto>> getByPet(@PathVariable Long petId) {
        return ResponseEntity.ok(reportService.getByPet(petId));
    }

    @PostMapping
    public ResponseEntity<ReportDto> create(@Valid @RequestBody ReportDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.createReport(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReportDto> update(
            @PathVariable Long id,
            @RequestBody ReportDto dto,
            @RequestHeader(value = "X-User-Id", defaultValue = "0") String requestingUserId,
            @RequestHeader(value = "X-User-Role", defaultValue = "ADMIN") String requestingUserRole
    ) {
        return ResponseEntity.ok(
                reportService.updateReport(id, dto, Long.parseLong(requestingUserId), requestingUserRole)
        );
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ReportDto> updateEstado(
            @PathVariable Long id,
            @RequestParam String estado,
            @RequestHeader(value = "X-User-Id", defaultValue = "0") String requestingUserId,
            @RequestHeader(value = "X-User-Role", defaultValue = "ADMIN") String requestingUserRole
    ) {
        return ResponseEntity.ok(
                reportService.updateEstado(id, estado, Long.parseLong(requestingUserId), requestingUserRole)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "0") String requestingUserId,
            @RequestHeader(value = "X-User-Role", defaultValue = "ADMIN") String requestingUserRole
    ) {
        reportService.deleteReport(id, Long.parseLong(requestingUserId), requestingUserRole);
        return ResponseEntity.noContent().build();
    }
}