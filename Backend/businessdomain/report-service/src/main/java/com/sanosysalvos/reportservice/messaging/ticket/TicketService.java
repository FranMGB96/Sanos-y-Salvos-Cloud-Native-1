package com.sanosysalvos.reportservice.messaging.ticket;

import com.sanosysalvos.reportservice.messaging.event.TicketRequestedEvent;
import com.sanosysalvos.reportservice.messaging.support.InvalidMessageException;
import com.sanosysalvos.reportservice.messaging.support.TransientProcessingException;
import com.sanosysalvos.reportservice.model.Report;
import com.sanosysalvos.reportservice.repository.ReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Lógica del caso 2: generar el ticket (comprobante) de un reporte. */
@Service
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    private final ReportRepository reportRepository;

    @Value("${sanosysalvos.messaging.demo.failure-marker:[FALLO-TEMPORAL]}")
    private String failureMarker;

    public TicketService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public String generateTicket(TicketRequestedEvent event) {
        if (event.getReportId() == null) {
            throw new InvalidMessageException("reportId es obligatorio");
        }
        Report report = reportRepository.findById(event.getReportId())
                .orElseThrow(() -> new InvalidMessageException(
                        "El reporte " + event.getReportId() + " no existe"));

        // Solo DEMO: simula un fallo temporal para mostrar reintentos -> DLQ
        if (event.getDescripcion() != null && !failureMarker.isBlank()
                && event.getDescripcion().contains(failureMarker)) {
            throw new TransientProcessingException("Generador de documentos simulado no disponible");
        }

        String code = "TCK-" + report.getId() + "-"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        log.info("[TICKET] Comprobante {} generado | reporte #{} | tipo={} | usuario={}",
                code, report.getId(), report.getTipo(), report.getReporterUserId());
        return code;
    }
}
