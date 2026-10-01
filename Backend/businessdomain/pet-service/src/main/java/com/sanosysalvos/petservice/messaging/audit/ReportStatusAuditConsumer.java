package com.sanosysalvos.petservice.messaging.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.sanosysalvos.petservice.messaging.support.AckSupport;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Consumidor del dominio "auditoría": escucha audit.report-status.queue. */
@Component
public class ReportStatusAuditConsumer {

    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    @Value("${sanosysalvos.messaging.retry.max-attempts:3}")
    private int maxAttempts;

    @Value("${sanosysalvos.messaging.retry.backoff-ms:1000}")
    private long backoffMs;

    public ReportStatusAuditConsumer(AuditService auditService, ObjectMapper objectMapper) {
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    // El nombre de la cola viene del application.yml compartido
    @RabbitListener(queues = "${sanosysalvos.messaging.queues.audit}")
    public void onStatusChanged(Message message, Channel channel) throws IOException {
        AckSupport.handle(message, channel, "auditoria", maxAttempts, backoffMs, () -> {
            ReportStatusChangedEvent event = AckSupport.parse(objectMapper, message, ReportStatusChangedEvent.class);
            auditService.auditStatusChange(event);
        });
    }
}
