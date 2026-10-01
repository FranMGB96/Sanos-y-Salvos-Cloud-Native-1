package com.sanosysalvos.petservice.messaging.audit;

import com.sanosysalvos.petservice.messaging.support.InvalidMessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;

/** Lógica del caso 3: dejar registro de auditoría de cada cambio de estado de un reporte. */
@Service
public class AuditService {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDITORIA");
    private static final Set<String> VALID_STATUS = Set.of("ACTIVO", "RESUELTO", "CERRADO");

    public void auditStatusChange(ReportStatusChangedEvent event) {
        if (event.getReportId() == null) {
            throw new InvalidMessageException("reportId es obligatorio");
        }
        if (event.getNewStatus() == null || event.getNewStatus().isBlank()) {
            throw new InvalidMessageException("newStatus es obligatorio");
        }
        if (!VALID_STATUS.contains(event.getNewStatus().toUpperCase())) {
            throw new InvalidMessageException("Estado desconocido: " + event.getNewStatus());
        }
        auditLog.info("Reporte #{}: {} -> {} (evento de {})",
                event.getReportId(), event.getPreviousStatus(), event.getNewStatus(), event.getOccurredAt());
    }
}
