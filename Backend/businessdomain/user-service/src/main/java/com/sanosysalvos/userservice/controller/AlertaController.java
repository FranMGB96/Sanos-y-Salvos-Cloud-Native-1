package com.sanosysalvos.userservice.controller;

import com.sanosysalvos.userservice.messaging.kafka.AlertaService;
import com.sanosysalvos.userservice.model.AlertaRecibida;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Alertas por zona que llegaron por Kafka (CU2). */
@RestController
@RequestMapping("/users/alertas")
@Tag(name = "Alertas por zona (Kafka)")
public class AlertaController {

    private final AlertaService alertaService;

    public AlertaController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @GetMapping
    @Operation(summary = "Últimas 20 alertas recibidas")
    public ResponseEntity<List<AlertaRecibida>> ultimas() {
        return ResponseEntity.ok(alertaService.ultimasAlertas());
    }
}
