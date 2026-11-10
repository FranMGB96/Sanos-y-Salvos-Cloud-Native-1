package com.sanosysalvos.petservice.controller;

import com.sanosysalvos.petservice.messaging.kafka.PetHistoryService;
import com.sanosysalvos.petservice.model.PetEventHistory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Historial de eventos de una mascota, armado a partir de lo que llega por Kafka (CU1). */
@RestController
@RequestMapping("/pets")
@Tag(name = "Historial de mascotas (Kafka)")
public class PetHistoryController {

    private final PetHistoryService petHistoryService;

    public PetHistoryController(PetHistoryService petHistoryService) {
        this.petHistoryService = petHistoryService;
    }

    @GetMapping("/{id}/historial")
    @Operation(summary = "Eventos de reportes de una mascota, del más reciente al más antiguo")
    public ResponseEntity<List<PetEventHistory>> historial(@PathVariable Long id) {
        return ResponseEntity.ok(petHistoryService.historialDeMascota(id));
    }
}
