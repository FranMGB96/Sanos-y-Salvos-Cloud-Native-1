package com.sanosysalvos.userservice;

import com.sanosysalvos.userservice.messaging.kafka.AlertaEvent;
import com.sanosysalvos.userservice.messaging.kafka.AlertaService;
import com.sanosysalvos.userservice.model.AlertaRecibida;
import com.sanosysalvos.userservice.repository.AlertaRecibidaRepository;
import com.sanosysalvos.userservice.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Pruebas unitarias del consumidor de alertas (lógica, sin Kafka). */
@ExtendWith(MockitoExtension.class)
class AlertaServiceTest {

    @Mock private AlertaRecibidaRepository alertaRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private AlertaService alertaService;

    private AlertaEvent alerta() {
        return AlertaEvent.builder().alertaId("al-1").zona("-33.45_-70.67").tipo("PERDIDO").cantidad(3)
                .ventanaMinutos(60).reportIds(List.of(15L, 16L, 17L)).severidad("MEDIA")
                .motivo("3 mascotas perdidas en la misma zona en la última hora")
                .timestamp("2026-11-05T21:05:02Z").build();
    }

    @Test
    @DisplayName("Alerta nueva -> se guarda con los ids de los reportes")
    void alertaNueva_seGuarda() {
        when(alertaRepository.existsByAlertaId("al-1")).thenReturn(false);
        when(userRepository.count()).thenReturn(4L);

        assertTrue(alertaService.procesar(alerta()));

        ArgumentCaptor<AlertaRecibida> captor = ArgumentCaptor.forClass(AlertaRecibida.class);
        verify(alertaRepository).save(captor.capture());
        assertEquals("15,16,17", captor.getValue().getReportIds());
        assertEquals("MEDIA", captor.getValue().getSeveridad());
    }

    @Test
    @DisplayName("Alerta repetida -> no se guarda ni se notifica de nuevo")
    void alertaRepetida_seIgnora() {
        when(alertaRepository.existsByAlertaId("al-1")).thenReturn(true);

        assertFalse(alertaService.procesar(alerta()));

        verify(alertaRepository, never()).save(any());
        verifyNoInteractions(userRepository);
    }
}
