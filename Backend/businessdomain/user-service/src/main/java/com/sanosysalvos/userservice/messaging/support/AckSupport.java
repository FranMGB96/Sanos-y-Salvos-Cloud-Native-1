package com.sanosysalvos.userservice.messaging.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;

import java.io.IOException;

/**
 * Manejo EXPLÍCITO de ACK / NACK / reintentos para los consumidores.
 *
 * Tres comportamientos distintos:
 *  1. ÉXITO                       -> basicAck.
 *  2. MENSAJE INVÁLIDO            -> basicNack(requeue=false) de inmediato -> DLQ (sin reintentos).
 *  3. ERROR TRANSITORIO / otro    -> se reintenta hasta maxAttempts (con espera creciente);
 *                                    si se agotan -> basicNack(requeue=false) -> DLQ.
 *
 * El mensaje permanece sin confirmar (unacked) mientras se reintenta; con
 * prefetch=1 el consumidor no recibe otro hasta resolver este. Los reintentos
 * son reales porque el código los controla aquí y no se traga la excepción.
 */
public final class AckSupport {

    private static final Logger log = LoggerFactory.getLogger(AckSupport.class);

    private AckSupport() {
    }

    public static void handle(Message message, Channel channel, String context,
                              int maxAttempts, long backoffMs, Runnable task) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        int attempt = 1;

        while (true) {
            try {
                task.run();
                break; // éxito: sale del ciclo y confirma abajo
            } catch (InvalidMessageException e) {
                log.warn("[{}] Mensaje INVÁLIDO, sin reintentos -> NACK sin requeue (DLQ). Motivo: {}",
                        context, e.getMessage());
                channel.basicNack(deliveryTag, false, false);
                return;
            } catch (Exception e) {
                if (attempt >= maxAttempts) {
                    log.error("[{}] Reintentos agotados ({}/{}) -> NACK sin requeue (DLQ). Último error: {}",
                            context, attempt, maxAttempts, e.getMessage());
                    channel.basicNack(deliveryTag, false, false);
                    return;
                }
                log.warn("[{}] Error transitorio (intento {}/{}): {}. Reintentando en {} ms",
                        context, attempt, maxAttempts, e.getMessage(), backoffMs * attempt);
                if (!pause(backoffMs * attempt)) {
                    // Interrumpido (apagado del servicio): devolver el mensaje a la cola
                    channel.basicNack(deliveryTag, false, true);
                    return;
                }
                attempt++;
            }
        }

        channel.basicAck(deliveryTag, false);
        log.info("[{}] Mensaje procesado -> ACK (intento {})", context, attempt);
    }

    /** Convierte el cuerpo JSON del mensaje en el evento; si no se puede, es mensaje inválido. */
    public static <T> T parse(ObjectMapper mapper, Message message, Class<T> type) {
        byte[] body = message.getBody();
        if (body == null || body.length == 0) {
            throw new InvalidMessageException("El mensaje no tiene cuerpo");
        }
        try {
            T value = mapper.readValue(body, type);
            if (value == null) {
                throw new InvalidMessageException("El cuerpo del mensaje es null");
            }
            return value;
        } catch (IOException e) {
            throw new InvalidMessageException("JSON inválido: " + e.getMessage());
        }
    }

    private static boolean pause(long millis) {
        try {
            Thread.sleep(millis);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
