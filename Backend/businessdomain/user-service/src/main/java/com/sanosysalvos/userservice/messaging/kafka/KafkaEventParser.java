package com.sanosysalvos.userservice.messaging.kafka;

import com.sanosysalvos.userservice.messaging.support.InvalidMessageException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Convierte el texto JSON que llega por Kafka en el DTO y lo valida.
 *
 * El listener recibe el mensaje como texto (no como objeto) a propósito:
 * si el mensaje es inválido, el que termina en el Dead Letter Topic es el
 * texto ORIGINAL, tal como llegó, y no un objeto con los campos en null.
 * Así se puede revisar después qué mandó realmente el productor.
 */
@Component
public class KafkaEventParser {

    private final ObjectMapper objectMapper;
    private final Validator validator;

    public KafkaEventParser(ObjectMapper objectMapper, Validator validator) {
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    /** @throws InvalidMessageException si no es JSON o no cumple las validaciones (va directo al DLT) */
    public <T> T parse(String json, Class<T> tipo) {
        if (json == null || json.isBlank()) {
            throw new InvalidMessageException("mensaje vacío");
        }
        T objeto;
        try {
            objeto = objectMapper.readValue(json, tipo);
        } catch (JsonProcessingException e) {
            throw new InvalidMessageException("no es un JSON válido");
        }
        Set<ConstraintViolation<T>> errores = validator.validate(objeto);
        if (!errores.isEmpty()) {
            String campos = errores.stream()
                    .map(v -> v.getPropertyPath().toString())
                    .distinct()
                    .sorted()
                    .collect(Collectors.joining(", "));
            throw new InvalidMessageException("faltan o son inválidos los campos: " + campos);
        }
        return objeto;
    }
}
