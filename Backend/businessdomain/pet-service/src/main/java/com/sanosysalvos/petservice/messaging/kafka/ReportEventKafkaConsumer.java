package com.sanosysalvos.petservice.messaging.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Consumidor de sys.reports.events en pet-service (grupo pet-history-group).
 *
 * - concurrency = 3: tres consumidores en el mismo grupo, uno por partición (balanceo de carga).
 * - Es un grupo distinto al de analytics-service, por eso los dos leen TODOS los eventos
 *   de forma independiente.
 * - Recibe el mensaje como texto y KafkaEventParser lo convierte y valida. Si es inválido,
 *   va directo al DLT con el texto original (no se reintenta).
 * - Si el procesamiento falla por algo temporal, el error handler reintenta con backoff
 *   y después lo manda a sys.reports.events.DLT.
 */
@Component
public class ReportEventKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(ReportEventKafkaConsumer.class);

    private final PetHistoryService petHistoryService;
    private final KafkaEventParser parser;

    public ReportEventKafkaConsumer(PetHistoryService petHistoryService, KafkaEventParser parser) {
        this.petHistoryService = petHistoryService;
        this.parser = parser;
    }

    @KafkaListener(
            id = "pet-history-listener",
            topics = "${sanosysalvos.kafka.topics.report-events}",
            groupId = "pet-history-group",
            concurrency = "3",
            autoStartup = "${sanosysalvos.kafka.enabled:true}",
            properties = "spring.deserializer.value.delegate.class=org.apache.kafka.common.serialization.StringDeserializer")
    public void onReportEvent(@Payload String json,
                              @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                              @Header(KafkaHeaders.OFFSET) long offset) {
        ReportEvent event = parser.parse(json, ReportEvent.class);
        MDC.put("correlationId", event.getEventId());
        try {
            log.info("[Kafka] Recibido {} del reporte #{} (particion={}, offset={})",
                    event.getTipoEvento(), event.getReportId(), partition, offset);
            petHistoryService.registrar(event);
        } finally {
            MDC.remove("correlationId");
        }
    }
}
