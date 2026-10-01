package com.sanosysalvos.reportservice.messaging.ticket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.sanosysalvos.reportservice.messaging.event.TicketRequestedEvent;
import com.sanosysalvos.reportservice.messaging.support.AckSupport;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Consumidor del dominio "tickets": escucha ticket.generation.queue. */
@Component
public class TicketConsumer {

    private final TicketService ticketService;
    private final ObjectMapper objectMapper;

    @Value("${sanosysalvos.messaging.retry.max-attempts:3}")
    private int maxAttempts;

    @Value("${sanosysalvos.messaging.retry.backoff-ms:1000}")
    private long backoffMs;

    public TicketConsumer(TicketService ticketService, ObjectMapper objectMapper) {
        this.ticketService = ticketService;
        this.objectMapper = objectMapper;
    }

    // El nombre de la cola viene del application.yml compartido
    @RabbitListener(queues = "${sanosysalvos.messaging.queues.ticket}")
    public void onTicketRequested(Message message, Channel channel) throws IOException {
        AckSupport.handle(message, channel, "ticket", maxAttempts, backoffMs, () -> {
            TicketRequestedEvent event = AckSupport.parse(objectMapper, message, TicketRequestedEvent.class);
            ticketService.generateTicket(event);
        });
    }
}
