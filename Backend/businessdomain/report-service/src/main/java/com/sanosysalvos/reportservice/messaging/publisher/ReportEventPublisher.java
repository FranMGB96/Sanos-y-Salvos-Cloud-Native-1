package com.sanosysalvos.reportservice.messaging.publisher;

import com.sanosysalvos.reportservice.config.MessagingProperties;
import com.sanosysalvos.reportservice.messaging.event.ReportCreatedEvent;
import com.sanosysalvos.reportservice.messaging.event.ReportStatusChangedEvent;
import com.sanosysalvos.reportservice.messaging.event.TicketRequestedEvent;
import com.sanosysalvos.reportservice.model.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * ÚNICO punto de publicación de eventos de reportes hacia RabbitMQ.
 *
 * Se llama DESPUÉS de guardar en la base de datos. Si RabbitMQ no está
 * disponible, el error se registra y la operación del reporte continúa
 * normalmente: la mensajería no puede romper la lógica existente.
 */
@Component
public class ReportEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ReportEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final MessagingProperties props;

    public ReportEventPublisher(RabbitTemplate rabbitTemplate, MessagingProperties props) {
        this.rabbitTemplate = rabbitTemplate;
        this.props = props;
    }

    /** Caso 1: topic sys.reports.topic, key report.created.perdido | report.created.encontrado */
    public void publishReportCreated(Report report) {
        ReportCreatedEvent event = ReportCreatedEvent.builder()
                .reportId(report.getId())
                .tipo(report.getTipo().name())
                .descripcion(report.getDescripcion())
                .reporterUserId(report.getReporterUserId())
                .petId(report.getPetId())
                .occurredAt(LocalDateTime.now().toString())
                .build();
        String routingKey = props.getRoutingKeys().getReportCreatedPrefix()
                + report.getTipo().name().toLowerCase();
        send(props.getExchanges().getReportsTopic(), routingKey, event,
                "reporte creado #" + report.getId());
    }

    /** Caso 2: direct sys.tickets.direct, key ticket.generate */
    public void publishTicketRequested(Report report) {
        TicketRequestedEvent event = TicketRequestedEvent.builder()
                .reportId(report.getId())
                .tipo(report.getTipo().name())
                .descripcion(report.getDescripcion())
                .reporterUserId(report.getReporterUserId())
                .occurredAt(LocalDateTime.now().toString())
                .build();
        send(props.getExchanges().getTicketsDirect(),
                props.getRoutingKeys().getTicketGenerate(), event,
                "ticket del reporte #" + report.getId());
    }

    /** Caso 3: topic sys.reports.topic, key report.status.<estado>. Solo si el estado cambió. */
    public void publishStatusChanged(Report report, Report.EstadoReporte previousStatus) {
        if (previousStatus == report.getEstado()) {
            return;
        }
        ReportStatusChangedEvent event = ReportStatusChangedEvent.builder()
                .reportId(report.getId())
                .previousStatus(previousStatus == null ? null : previousStatus.name())
                .newStatus(report.getEstado().name())
                .occurredAt(LocalDateTime.now().toString())
                .build();
        String routingKey = props.getRoutingKeys().getStatusPrefix()
                + report.getEstado().name().toLowerCase();
        send(props.getExchanges().getReportsTopic(), routingKey, event,
                "cambio de estado del reporte #" + report.getId());
    }

    private void send(String exchange, String routingKey, Object payload, String description) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, payload);
            log.info("[RabbitMQ] Publicado {} -> exchange='{}' routingKey='{}'",
                    description, exchange, routingKey);
        } catch (Exception e) {
            log.error("[RabbitMQ] No se pudo publicar {} (exchange='{}', key='{}'). "
                    + "La operación principal continúa. Causa: {}",
                    description, exchange, routingKey, e.getMessage());
        }
    }
}
