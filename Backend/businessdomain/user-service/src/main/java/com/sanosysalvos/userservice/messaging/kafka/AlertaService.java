package com.sanosysalvos.userservice.messaging.kafka;

import com.sanosysalvos.userservice.model.AlertaRecibida;
import com.sanosysalvos.userservice.repository.AlertaRecibidaRepository;
import com.sanosysalvos.userservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Lógica del CU2 en user-service: registrar la alerta y avisar a los usuarios
 * (aviso simulado con un log, igual que el correo simulado de RabbitMQ).
 */
@Service
public class AlertaService {

    private static final Logger log = LoggerFactory.getLogger(AlertaService.class);

    private final AlertaRecibidaRepository alertaRepository;
    private final UserRepository userRepository;

    public AlertaService(AlertaRecibidaRepository alertaRepository, UserRepository userRepository) {
        this.alertaRepository = alertaRepository;
        this.userRepository = userRepository;
    }

    /** @return true si la alerta era nueva y se notificó, false si era repetida */
    public boolean procesar(AlertaEvent alerta) {
        if (alertaRepository.existsByAlertaId(alerta.getAlertaId())) {
            log.info("[Kafka] Alerta {} ya recibida antes (duplicada), se ignora", alerta.getAlertaId());
            return false;
        }
        String reportIds = alerta.getReportIds() == null ? null
                : alerta.getReportIds().stream().map(String::valueOf).collect(Collectors.joining(","));
        try {
            alertaRepository.save(AlertaRecibida.builder()
                    .alertaId(alerta.getAlertaId())
                    .zona(alerta.getZona())
                    .tipo(alerta.getTipo())
                    .cantidad(alerta.getCantidad())
                    .severidad(alerta.getSeveridad())
                    .motivo(alerta.getMotivo())
                    .reportIds(reportIds)
                    .generadaEn(alerta.getTimestamp())
                    .build());
        } catch (DataIntegrityViolationException duplicada) {
            log.info("[Kafka] Alerta {} duplicada (restricción única), se ignora", alerta.getAlertaId());
            return false;
        }
        long usuarios = userRepository.count();
        log.warn("[AVISO SIMULADO] Alerta {} en zona {}: {} | se avisa a {} usuarios | reportes: {}",
                alerta.getSeveridad(), alerta.getZona(), alerta.getMotivo(), usuarios, reportIds);
        return true;
    }

    public List<AlertaRecibida> ultimasAlertas() {
        return alertaRepository.findTop20ByOrderByIdDesc();
    }
}
