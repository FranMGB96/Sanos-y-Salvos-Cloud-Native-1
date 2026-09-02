package com.sanosysalvos.bff.controller;

import com.sanosysalvos.bff.dto.DashboardDto;
import com.sanosysalvos.bff.dto.ReporteConDetalleDto;
import com.sanosysalvos.bff.dto.UsuarioConMascotasDto;
import com.sanosysalvos.bff.service.BffService;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/bff")
@Tag(name = "BFF", description = "Backend For Frontend")
public class BffController {

    @Autowired
    private BffService bffService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDto> getDashboard() {
        return ResponseEntity.ok(bffService.getDashboard());
    }

    @GetMapping("/usuarios/{userId}/mascotas")
    public ResponseEntity<UsuarioConMascotasDto> getUsuarioConMascotas(
            @PathVariable Long userId) {
        return ResponseEntity.ok(bffService.getUsuarioConMascotas(userId));
    }

    @GetMapping("/reportes")
    public ResponseEntity<List<ReporteConDetalleDto>> getReportes() {
        return ResponseEntity.ok(bffService.getReportesConDetalle());
    }

    @GetMapping("/reportes/tipo/{tipo}")
    public ResponseEntity<List<ReporteConDetalleDto>> getReportesPorTipo(
            @PathVariable String tipo) {
        return ResponseEntity.ok(bffService.getReportesPorTipo(tipo));
    }

    @GetMapping("/reportes/usuario/{userId}")
    public ResponseEntity<List<ReporteConDetalleDto>> getReportesPorUsuario(
            @PathVariable Long userId) {
        return ResponseEntity.ok(bffService.getReportesPorUsuario(userId));
    }
}