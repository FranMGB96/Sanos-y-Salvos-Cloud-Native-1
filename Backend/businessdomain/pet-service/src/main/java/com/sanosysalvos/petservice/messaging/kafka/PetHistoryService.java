package com.sanosysalvos.petservice.messaging.kafka;

import com.sanosysalvos.petservice.messaging.support.InvalidMessageException;
import com.sanosysalvos.petservice.model.PetEventHistory;
import com.sanosysalvos.petservice.repository.PetEventHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Lógica del historial de mascotas (CU1). El listener solo recibe el mensaje
 * y llama a este servicio: la lógica de negocio queda separada de la mensajería.
 */
@Service
public class PetHistoryService {

    private static final Logger log = LoggerFactory.getLogger(PetHistoryService.class);

    private final PetEventHistoryRepository repository;

    public PetHistoryService(PetEventHistoryRepository repository) {
        this.repository = repository;
    }

    /**
     * Registra el evento en el historial de la mascota.
     * @return true si se guardó, false si se ignoró (sin mascota o duplicado)
     */
    public boolean registrar(ReportEvent event) {
        if ("ESTADO_CAMBIADO".equals(event.getTipoEvento()) && event.getEstadoAnterior() == null) {
            // Un cambio de estado sin estado anterior no tiene sentido: reintentar no lo arregla
            throw new InvalidMessageException("ESTADO_CAMBIADO sin estadoAnterior (reporte #" + event.getReportId() + ")");
        }
        if (event.getPetId() == null) {
            log.info("[Kafka] Reporte #{} sin mascota asociada: no se agrega al historial", event.getReportId());
            return false;
        }
        // Idempotencia: Kafka garantiza "al menos una vez", así que un evento puede llegar repetido
        if (repository.existsByEventId(event.getEventId())) {
            log.info("[Kafka] Evento {} ya estaba registrado (duplicado), se ignora", event.getEventId());
            return false;
        }
        try {
            repository.save(PetEventHistory.builder()
                    .eventId(event.getEventId())
                    .petId(event.getPetId())
                    .reportId(event.getReportId())
                    .tipoEvento(event.getTipoEvento())
                    .tipo(event.getTipo())
                    .estadoAnterior(event.getEstadoAnterior())
                    .estado(event.getEstado())
                    .ocurridoEn(event.getTimestamp())
                    .build());
        } catch (DataIntegrityViolationException duplicado) {
            // Dos consumidores procesaron el mismo evento al mismo tiempo: la restricción UNIQUE lo frena
            log.info("[Kafka] Evento {} duplicado (restricción única), se ignora", event.getEventId());
            return false;
        }
        log.info("[Kafka] Historial de la mascota #{}: {} del reporte #{} ({} -> {})",
                event.getPetId(), event.getTipoEvento(), event.getReportId(),
                event.getEstadoAnterior(), event.getEstado());
        return true;
    }

    public List<PetEventHistory> historialDeMascota(Long petId) {
        return repository.findByPetIdOrderByIdDesc(petId);
    }
}
