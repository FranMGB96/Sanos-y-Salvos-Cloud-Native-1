package com.sanosysalvos.petservice;

import com.sanosysalvos.petservice.messaging.kafka.PetHistoryService;
import com.sanosysalvos.petservice.messaging.kafka.ReportEvent;
import com.sanosysalvos.petservice.messaging.support.InvalidMessageException;
import com.sanosysalvos.petservice.model.PetEventHistory;
import com.sanosysalvos.petservice.repository.PetEventHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Pruebas unitarias del consumidor de historial (lógica, sin Kafka). */
@ExtendWith(MockitoExtension.class)
class PetHistoryServiceTest {

    @Mock private PetEventHistoryRepository repository;
    @InjectMocks private PetHistoryService service;

    private ReportEvent evento(Long petId, String tipoEvento, String estadoAnterior) {
        return ReportEvent.builder().eventId("evt-1").tipoEvento(tipoEvento).reportId(17L).petId(petId)
                .tipo("PERDIDO").estadoAnterior(estadoAnterior).estado("ACTIVO")
                .timestamp("2026-11-05T21:05:01Z").build();
    }

    @Test
    @DisplayName("Evento nuevo con mascota -> se guarda en el historial")
    void eventoNuevo_seGuarda() {
        when(repository.existsByEventId("evt-1")).thenReturn(false);
        assertTrue(service.registrar(evento(5L, "REPORTE_CREADO", null)));
        verify(repository).save(any(PetEventHistory.class));
    }

    @Test
    @DisplayName("Evento repetido (mismo eventId) -> se ignora (idempotencia)")
    void eventoDuplicado_seIgnora() {
        when(repository.existsByEventId("evt-1")).thenReturn(true);
        assertFalse(service.registrar(evento(5L, "REPORTE_CREADO", null)));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Reporte sin mascota -> no se agrega al historial")
    void sinMascota_seIgnora() {
        assertFalse(service.registrar(evento(null, "REPORTE_CREADO", null)));
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Cambio de estado sin estado anterior -> mensaje inválido (va al DLT)")
    void cambioSinEstadoAnterior_esInvalido() {
        assertThrows(InvalidMessageException.class,
                () -> service.registrar(evento(5L, "ESTADO_CAMBIADO", null)));
    }
}
