package com.sanosysalvos.analyticsservice.controller;

import com.sanosysalvos.analyticsservice.model.AlertaEvent;
import com.sanosysalvos.analyticsservice.model.ResumenReportes;
import com.sanosysalvos.analyticsservice.streams.AnalyticsQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** API que usa el "Panel de actividad" del frontend (a través del api-gateway: /api/analytics/**). */
@RestController
@RequestMapping("/analytics")
@Tag(name = "Analytics (Kafka Streams)")
public class AnalyticsController {

    private final AnalyticsQueryService queryService;

    public AnalyticsController(AnalyticsQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/resumen")
    @Operation(summary = "Contadores en vivo de reportes (desde que se activó Kafka)")
    public ResponseEntity<ResumenReportes> resumen() {
        return ResponseEntity.ok(queryService.resumen());
    }

    @GetMapping("/alertas")
    @Operation(summary = "Última alerta de cada zona")
    public ResponseEntity<List<AlertaEvent>> alertas() {
        return ResponseEntity.ok(queryService.alertas());
    }

    @GetMapping("/estado")
    @Operation(summary = "Estado de Kafka Streams (RUNNING, REBALANCING, ...)")
    public ResponseEntity<Map<String, String>> estado() {
        return ResponseEntity.ok(Map.of("estado", queryService.estado()));
    }

    @ExceptionHandler(AnalyticsQueryService.StreamsNoDisponibleException.class)
    public ResponseEntity<Map<String, String>> noDisponible(AnalyticsQueryService.StreamsNoDisponibleException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("estado", queryService.estado(), "mensaje", e.getMessage()));
    }
}
