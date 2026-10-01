package com.sanosysalvos.userservice.messaging.notification;

import com.sanosysalvos.userservice.messaging.support.InvalidMessageException;
import com.sanosysalvos.userservice.messaging.support.TransientProcessingException;
import com.sanosysalvos.userservice.model.User;
import com.sanosysalvos.userservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Lógica del caso 1: avisar por correo (simulado) que el reporte fue registrado. */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final UserRepository userRepository;

    @Value("${sanosysalvos.messaging.demo.failure-marker:[FALLO-TEMPORAL]}")
    private String failureMarker;

    public NotificationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void notifyReportCreated(ReportCreatedEvent event) {
        // 1) Validación: un mensaje incompleto es INVÁLIDO (va directo a la DLQ)
        if (event.getReportId() == null) {
            throw new InvalidMessageException("reportId es obligatorio");
        }
        if (event.getReporterUserId() == null) {
            throw new InvalidMessageException("reporterUserId es obligatorio");
        }
        if (event.getTipo() == null || event.getTipo().isBlank()) {
            throw new InvalidMessageException("tipo es obligatorio");
        }

        // 2) El usuario debe existir (si no, reintentar no lo arregla)
        User user = userRepository.findById(event.getReporterUserId())
                .orElseThrow(() -> new InvalidMessageException(
                        "El usuario " + event.getReporterUserId() + " no existe"));

        // 3) Solo DEMO: simula un fallo temporal del servidor de correo
        if (event.getDescripcion() != null && !failureMarker.isBlank()
                && event.getDescripcion().contains(failureMarker)) {
            throw new TransientProcessingException("Servidor de correo simulado no disponible");
        }

        // 4) "Envío" de correo (simulado con un log)
        log.info("[EMAIL SIMULADO] Para: {} | Asunto: Tu reporte #{} fue registrado | Detalle: reporte de mascota {}",
                user.getEmail(), event.getReportId(), event.getTipo());
    }
}
