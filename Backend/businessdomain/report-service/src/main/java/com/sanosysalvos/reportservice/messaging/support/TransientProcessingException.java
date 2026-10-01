package com.sanosysalvos.reportservice.messaging.support;

/**
 * Error TRANSITORIO (servicio externo caído, timeout, fallo temporal).
 * Reintentar puede funcionar: AckSupport lo reintenta hasta el máximo
 * configurado y, si se agota, manda el mensaje a la DLQ.
 */
public class TransientProcessingException extends RuntimeException {
    public TransientProcessingException(String message) {
        super(message);
    }
}
