package com.sanosysalvos.reportservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topología de RabbitMQ del sistema (Evaluación 2).
 *
 * Casos de uso:
 *  1. Notificar reporte creado  -> exchange TOPIC  sys.reports.topic  -> cola notification.report-created.queue
 *  2. Generar ticket            -> exchange DIRECT sys.tickets.direct -> cola ticket.generation.queue
 *  3. Auditar cambio de estado  -> exchange TOPIC  sys.reports.topic  -> cola audit.report-status.queue
 *
 * Cada cola tiene su DLQ: los mensajes rechazados (NACK sin requeue) van
 * al exchange sys.dlx (direct), que los reparte a la DLQ de su cola.
 *
 * Todos los nombres vienen de MessagingProperties (application.yml compartido).
 */
@Configuration
@EnableConfigurationProperties(MessagingProperties.class)
public class RabbitConfig {

    private final MessagingProperties props;

    public RabbitConfig(MessagingProperties props) {
        this.props = props;
    }

    // ───────────────────────── EXCHANGES ─────────────────────────

    /** TOPIC: enruta por patrón (report.created.# y report.status.*). */
    @Bean
    public TopicExchange reportsExchange() {
        return new TopicExchange(props.getExchanges().getReportsTopic(), true, false);
    }

    /** DIRECT: enruta por coincidencia exacta de la routing key (ticket.generate). */
    @Bean
    public DirectExchange ticketsExchange() {
        return new DirectExchange(props.getExchanges().getTicketsDirect(), true, false);
    }

    /** Dead Letter Exchange (direct): recibe los mensajes rechazados. */
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(props.getExchanges().getDlx(), true, false);
    }

    // ─────────────── COLAS PRINCIPALES (con DLX configurado) ───────────────

    /** Caso 1: notificación de reporte creado. */
    @Bean
    public Queue reportCreatedQueue() {
        return QueueBuilder.durable(props.getQueues().getReportCreated())
                .deadLetterExchange(props.getExchanges().getDlx())
                .deadLetterRoutingKey(props.getRoutingKeys().getDlqReportCreated())
                .build();
    }

    /** Caso 2: generación de ticket. */
    @Bean
    public Queue ticketQueue() {
        return QueueBuilder.durable(props.getQueues().getTicket())
                .deadLetterExchange(props.getExchanges().getDlx())
                .deadLetterRoutingKey(props.getRoutingKeys().getDlqTicket())
                .build();
    }

    /** Caso 3: auditoría de cambio de estado. */
    @Bean
    public Queue auditQueue() {
        return QueueBuilder.durable(props.getQueues().getAudit())
                .deadLetterExchange(props.getExchanges().getDlx())
                .deadLetterRoutingKey(props.getRoutingKeys().getDlqAudit())
                .build();
    }

    // ───────────────────────── DLQ ─────────────────────────

    @Bean
    public Queue reportCreatedDlq() {
        return QueueBuilder.durable(props.getQueues().getReportCreatedDlq()).build();
    }

    @Bean
    public Queue ticketDlq() {
        return QueueBuilder.durable(props.getQueues().getTicketDlq()).build();
    }

    @Bean
    public Queue auditDlq() {
        return QueueBuilder.durable(props.getQueues().getAuditDlq()).build();
    }

    // ───────────────────── BINDINGS (colas principales) ─────────────────────

    @Bean
    public Binding reportCreatedBinding() {
        return BindingBuilder.bind(reportCreatedQueue())
                .to(reportsExchange())
                .with(props.getRoutingKeys().getReportCreatedPattern());
    }

    @Bean
    public Binding ticketBinding() {
        return BindingBuilder.bind(ticketQueue())
                .to(ticketsExchange())
                .with(props.getRoutingKeys().getTicketGenerate());
    }

    @Bean
    public Binding auditBinding() {
        return BindingBuilder.bind(auditQueue())
                .to(reportsExchange())
                .with(props.getRoutingKeys().getStatusPattern());
    }

    // ───────────────────── BINDINGS (DLQ al DLX) ─────────────────────

    @Bean
    public Binding reportCreatedDlqBinding() {
        return BindingBuilder.bind(reportCreatedDlq())
                .to(deadLetterExchange())
                .with(props.getRoutingKeys().getDlqReportCreated());
    }

    @Bean
    public Binding ticketDlqBinding() {
        return BindingBuilder.bind(ticketDlq())
                .to(deadLetterExchange())
                .with(props.getRoutingKeys().getDlqTicket());
    }

    @Bean
    public Binding auditDlqBinding() {
        return BindingBuilder.bind(auditDlq())
                .to(deadLetterExchange())
                .with(props.getRoutingKeys().getDlqAudit());
    }

    // ───────────────────── CONVERSIÓN A JSON ─────────────────────

    /** Los mensajes viajan como JSON (no como objetos Java serializados). */
    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
