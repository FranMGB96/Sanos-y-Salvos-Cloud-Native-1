package com.sanosysalvos.kafkaadmin.dto;

/** Reglas de validación compartidas por los DTO. */
public final class ValidationPatterns {

    /** Nombre de topic válido en Kafka: letras, números, punto, guion y guion bajo (máx. 249). */
    public static final String TOPIC_NAME = "^[a-zA-Z0-9._-]{1,249}$";
    public static final String CLEANUP_POLICY = "^(delete|compact|compact,delete|delete,compact)$";

    private ValidationPatterns() {
    }
}
