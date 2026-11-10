package com.sanosysalvos.userservice.messaging.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Consumidor de sys.reports.alerts en user-service (grupo user-alerts-group).
 * Tres hilos (concurrency = 3) para repartir las 3 particiones del topic.
 * El listener solo recibe, convierte y valida; la lógica está en AlertaService.
 * Un mensaje inválido va directo al DLT con su texto original.
 */
@Component
public class AlertaKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(AlertaKafkaConsumer.class);

    private final AlertaService alertaService;
    private final KafkaEventParser parser;

    public AlertaKafkaConsumer(AlertaService alertaService, KafkaEventParser parser) {
        this.alertaService = alertaService;
        this.parser = parser;
    }

    @KafkaListener(
            id = "user-alerts-listener",
            topics = "${sanosysalvos.kafka.topics.alerts}",
            groupId = "user-alerts-group",
            concurrency = "3",
            autoStartup = "${sanosysalvos.kafka.enabled:true}",
            properties = "spring.deserializer.value.delegate.class=org.apache.kafka.common.serialization.StringDeserializer")
    public void onAlerta(@Payload String json,
                         @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                         @Header(KafkaHeaders.OFFSET) long offset) {
        AlertaEvent alerta = parser.parse(json, AlertaEvent.class);
        MDC.put("correlationId", alerta.getAlertaId());
        try {
            log.info("[Kafka] Alerta recibida para la zona {} (particion={}, offset={})",
                    alerta.getZona(), partition, offset);
            alertaService.procesar(alerta);
        } finally {
            MDC.remove("correlationId");
        }
    }
}
