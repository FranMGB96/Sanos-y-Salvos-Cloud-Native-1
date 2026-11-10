package com.sanosysalvos.reportservice.messaging.kafka;

import com.sanosysalvos.reportservice.model.Report;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

/**
 * Productor de Kafka del report-service (CU1: seguimiento de reportes en tiempo real).
 *
 * Publica en sys.reports.events cada reporte creado y cada cambio de estado.
 * Igual que el publicador de RabbitMQ, se llama después de guardar en la base de datos
 * y nunca lanza excepciones hacia afuera: si Kafka está caído, se registra el error y
 * el reporte se crea igual. Así Kafka no afecta lo que ya funcionaba con RabbitMQ.
 */
@Component
public class KafkaReportEventProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaReportEventProducer.class);
    public static final String CORRELATION_HEADER = "X-Correlation-Id";

    private final KafkaTemplate<String, ReportEvent> kafkaTemplate;
    private final String topic;
    private final boolean enabled;

    public KafkaReportEventProducer(KafkaTemplate<String, ReportEvent> kafkaTemplate,
                                    @Value("${sanosysalvos.kafka.topics.report-events}") String topic,
                                    @Value("${sanosysalvos.kafka.enabled:true}") boolean enabled) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
        this.enabled = enabled;
    }

    public void publishReportCreated(Report report) {
        send(buildEvent(report, ReportEvent.REPORTE_CREADO, null));
    }

    /** Solo publica si el estado realmente cambió. */
    public void publishStatusChanged(Report report, Report.EstadoReporte estadoAnterior) {
        if (estadoAnterior == report.getEstado()) {
            return;
        }
        send(buildEvent(report, ReportEvent.ESTADO_CAMBIADO,
                estadoAnterior == null ? null : estadoAnterior.name()));
    }

    ReportEvent buildEvent(Report report, String tipoEvento, String estadoAnterior) {
        return ReportEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .tipoEvento(tipoEvento)
                .reportId(report.getId())
                .petId(report.getPetId())
                .tipo(report.getTipo().name())
                .estadoAnterior(estadoAnterior)
                .estado(report.getEstado().name())
                .latitud(report.getLatitud())
                .longitud(report.getLongitud())
                .reporterUserId(report.getReporterUserId())
                .timestamp(Instant.now().toString())
                .build();
    }

    private void send(ReportEvent event) {
        if (!enabled) {
            return;
        }
        try {
            String key = String.valueOf(event.getReportId());
            long eventTime = Instant.parse(event.getTimestamp()).toEpochMilli();
            // El timestamp del record es la hora del hecho: Kafka Streams lo usa para las ventanas de tiempo
            ProducerRecord<String, ReportEvent> record = new ProducerRecord<>(topic, null, eventTime, key, event);
            record.headers().add(CORRELATION_HEADER, event.getEventId().getBytes(StandardCharsets.UTF_8));

            kafkaTemplate.send(record).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("[Kafka] No se pudo publicar {} del reporte #{} (eventId={}). La operación principal continúa. Causa: {}",
                            event.getTipoEvento(), event.getReportId(), event.getEventId(), ex.getMessage());
                } else {
                    log.info("[Kafka] Publicado {} del reporte #{} -> topic='{}' particion={} offset={} (eventId={})",
                            event.getTipoEvento(), event.getReportId(), topic,
                            result.getRecordMetadata().partition(), result.getRecordMetadata().offset(),
                            event.getEventId());
                }
            });
        } catch (Exception e) {
            log.error("[Kafka] No se pudo publicar {} del reporte #{}. La operación principal continúa. Causa: {}",
                    event.getTipoEvento(), event.getReportId(), e.getMessage());
        }
    }
}
