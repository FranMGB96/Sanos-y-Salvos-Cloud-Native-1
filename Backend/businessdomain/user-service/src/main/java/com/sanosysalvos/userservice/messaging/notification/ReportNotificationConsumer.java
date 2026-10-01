package com.sanosysalvos.userservice.messaging.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.sanosysalvos.userservice.messaging.support.AckSupport;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Consumidor del dominio "notificaciones": escucha notification.report-created.queue. */
@Component
public class ReportNotificationConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Value("${sanosysalvos.messaging.retry.max-attempts:3}")
    private int maxAttempts;

    @Value("${sanosysalvos.messaging.retry.backoff-ms:1000}")
    private long backoffMs;

    public ReportNotificationConsumer(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    // El nombre de la cola viene del application.yml compartido
    @RabbitListener(queues = "${sanosysalvos.messaging.queues.report-created}")
    public void onReportCreated(Message message, Channel channel) throws IOException {
        AckSupport.handle(message, channel, "notificacion", maxAttempts, backoffMs, () -> {
            ReportCreatedEvent event = AckSupport.parse(objectMapper, message, ReportCreatedEvent.class);
            notificationService.notifyReportCreated(event);
        });
    }
}
