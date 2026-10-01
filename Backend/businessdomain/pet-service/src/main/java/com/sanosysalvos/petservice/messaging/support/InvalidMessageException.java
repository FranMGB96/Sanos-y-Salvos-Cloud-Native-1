package com.sanosysalvos.petservice.messaging.support;

/**
 * Mensaje inválido o imposible de procesar (JSON roto, campos faltantes,
 * datos que no existen). Es un error PERMANENTE: reintentar no lo arregla,
 * así que el mensaje va directo a la DLQ (NACK sin requeue), sin reintentos.
 */
public class InvalidMessageException extends RuntimeException {
    public InvalidMessageException(String message) {
        super(message);
    }
}
