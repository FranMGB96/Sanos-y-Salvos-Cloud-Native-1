package com.sanosysalvos.rabbitadmin.service;

import com.sanosysalvos.rabbitadmin.dto.BindingRequest;
import com.sanosysalvos.rabbitadmin.dto.ExchangeRequest;
import com.sanosysalvos.rabbitadmin.dto.PublishRequest;
import com.sanosysalvos.rabbitadmin.dto.QueueRequest;
import com.sanosysalvos.rabbitadmin.exception.GlobalExceptionHandler;
import com.sanosysalvos.rabbitadmin.exception.InvalidRequestException;
import com.sanosysalvos.rabbitadmin.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** Operaciones sobre RabbitMQ usando AmqpAdmin (implementación RabbitAdmin). */
@Service
public class RabbitResourceService {

    private static final Logger log = LoggerFactory.getLogger(RabbitResourceService.class);

    private final AmqpAdmin amqpAdmin;
    private final RabbitTemplate rabbitTemplate;

    public RabbitResourceService(AmqpAdmin amqpAdmin, RabbitTemplate rabbitTemplate) {
        this.amqpAdmin = amqpAdmin;
        this.rabbitTemplate = rabbitTemplate;
    }

    // ───────────────────────── COLAS ─────────────────────────

    public Map<String, Object> createQueue(QueueRequest request) {
        boolean hasDlx = hasText(request.getDeadLetterExchange());
        if (!hasDlx && hasText(request.getDeadLetterRoutingKey())) {
            throw new InvalidRequestException("deadLetterRoutingKey requiere indicar también deadLetterExchange");
        }

        QueueBuilder builder = Boolean.FALSE.equals(request.getDurable())
                ? QueueBuilder.nonDurable(request.getName())
                : QueueBuilder.durable(request.getName());
        if (Boolean.TRUE.equals(request.getAutoDelete())) {
            builder.autoDelete();
        }
        if (request.getTtlMs() != null) {
            builder.ttl(request.getTtlMs());
        }
        if (hasDlx) {
            builder.deadLetterExchange(request.getDeadLetterExchange());
        }
        if (hasText(request.getDeadLetterRoutingKey())) {
            builder.deadLetterRoutingKey(request.getDeadLetterRoutingKey());
        }

        amqpAdmin.declareQueue(builder.build());
        log.info("Cola declarada: {}", request.getName());
        return result("Cola declarada correctamente", "name", request.getName());
    }

    public Map<String, Object> getQueue(String name) {
        QueueInformation info = amqpAdmin.getQueueInfo(name);
        if (info == null) {
            throw new ResourceNotFoundException("La cola '" + name + "' no existe");
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", info.getName());
        map.put("messageCount", info.getMessageCount());
        map.put("consumerCount", info.getConsumerCount());
        return map;
    }

    public Map<String, Object> deleteQueue(String name) {
        if (amqpAdmin.getQueueInfo(name) == null) {
            throw new ResourceNotFoundException("La cola '" + name + "' no existe");
        }
        amqpAdmin.deleteQueue(name);
        log.info("Cola eliminada: {}", name);
        return result("Cola eliminada correctamente", "name", name);
    }

    // ───────────────────────── EXCHANGES ─────────────────────────

    public Map<String, Object> createExchange(ExchangeRequest request) {
        ExchangeBuilder builder = switch (request.getType()) {
            case "direct" -> ExchangeBuilder.directExchange(request.getName());
            case "topic" -> ExchangeBuilder.topicExchange(request.getName());
            case "fanout" -> ExchangeBuilder.fanoutExchange(request.getName());
            case "headers" -> ExchangeBuilder.headersExchange(request.getName());
            default -> throw new InvalidRequestException("Tipo de exchange no soportado: " + request.getType());
        };
        builder.durable(!Boolean.FALSE.equals(request.getDurable()));
        if (Boolean.TRUE.equals(request.getAutoDelete())) {
            builder.autoDelete();
        }
        amqpAdmin.declareExchange(builder.build());
        log.info("Exchange declarado: {} ({})", request.getName(), request.getType());
        return result("Exchange declarado correctamente", "name", request.getName());
    }

    public Map<String, Object> deleteExchange(String name) {
        if (name.startsWith("amq.")) {
            throw new InvalidRequestException("Los exchanges que empiezan con 'amq.' son del sistema y no se pueden eliminar");
        }
        if (!exchangeExists(name)) {
            throw new ResourceNotFoundException("El exchange '" + name + "' no existe");
        }
        amqpAdmin.deleteExchange(name);
        log.info("Exchange eliminado: {}", name);
        return result("Exchange eliminado correctamente", "name", name);
    }

    // ───────────────────────── BINDINGS ─────────────────────────

    public Map<String, Object> createBinding(BindingRequest request) {
        amqpAdmin.declareBinding(toBinding(request));
        log.info("Binding declarado: {} -> {} ({})", request.getExchange(), request.getDestination(), request.getRoutingKey());
        return result("Binding declarado correctamente", "binding", describe(request));
    }

    public Map<String, Object> deleteBinding(BindingRequest request) {
        amqpAdmin.removeBinding(toBinding(request));
        log.info("Binding eliminado: {} -> {} ({})", request.getExchange(), request.getDestination(), request.getRoutingKey());
        return result("Binding eliminado correctamente", "binding", describe(request));
    }

    // ───────────────────────── MENSAJES DE PRUEBA ─────────────────────────

    /**
     * Publica un mensaje tal cual (sin conversión). Sirve para la demo: enviar un JSON
     * inválido o incompleto y ver cómo el consumidor lo manda a la DLQ.
     * Nota: si ninguna cola coincide con la routing key, RabbitMQ descarta el mensaje en silencio.
     */
    public Map<String, Object> publish(PublishRequest request) {
        Message message = MessageBuilder
                .withBody(request.getPayload().getBytes(StandardCharsets.UTF_8))
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .build();
        rabbitTemplate.send(request.getExchange(), request.getRoutingKey(), message);
        log.info("Mensaje de prueba publicado en exchange='{}' routingKey='{}'", request.getExchange(), request.getRoutingKey());
        return result("Mensaje publicado", "exchange", request.getExchange());
    }

    // ───────────────────────── Auxiliares ─────────────────────────

    private boolean exchangeExists(String name) {
        try {
            rabbitTemplate.execute(channel -> {
                channel.exchangeDeclarePassive(name);
                return Boolean.TRUE;
            });
            return true;
        } catch (AmqpException e) {
            if (GlobalExceptionHandler.rootMessage(e).contains("NOT_FOUND")) {
                return false;
            }
            throw e;
        }
    }

    private Binding toBinding(BindingRequest r) {
        Binding.DestinationType type = Binding.DestinationType.valueOf(r.getDestinationType());
        return new Binding(r.getDestination(), type, r.getExchange(), r.getRoutingKey(), null);
    }

    private String describe(BindingRequest r) {
        return r.getExchange() + " -> " + r.getDestination() + " [" + r.getDestinationType() + "] key='" + r.getRoutingKey() + "'";
    }

    private Map<String, Object> result(String message, String key, Object value) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("message", message);
        map.put(key, value);
        return map;
    }

    private boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
