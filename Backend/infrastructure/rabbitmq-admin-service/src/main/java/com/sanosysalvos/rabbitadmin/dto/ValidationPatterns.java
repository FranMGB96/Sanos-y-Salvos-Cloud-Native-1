package com.sanosysalvos.rabbitadmin.dto;

/** Expresiones regulares de validación compartidas por los DTOs y los controllers. */
public final class ValidationPatterns {

    /** Nombre para CREAR colas/exchanges: sin espacios y sin el prefijo reservado "amq.". */
    public static final String NEW_RESOURCE_NAME = "^(?!amq\\.)[A-Za-z0-9._-]{1,255}$";

    /** Nombre de un recurso ya existente (en bindings, publicación, consultas, eliminación). */
    public static final String RESOURCE_NAME = "^[A-Za-z0-9._-]{1,255}$";

    public static final String NAME_MESSAGE =
            "Nombre inválido: use solo letras, números, '.', '_' o '-' (1 a 255 caracteres, sin espacios; no puede empezar con 'amq.')";

    private ValidationPatterns() {
    }
}
