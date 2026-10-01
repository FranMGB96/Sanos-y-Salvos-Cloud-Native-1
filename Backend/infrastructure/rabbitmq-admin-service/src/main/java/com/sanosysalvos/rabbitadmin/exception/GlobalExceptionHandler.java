package com.sanosysalvos.rabbitadmin.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Traduce cada error a una respuesta JSON clara. NINGÚN error se traga en silencio:
 * validación -> 400, no existe -> 404, conflicto con RabbitMQ -> 409, RabbitMQ caído -> 503.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .sorted()
                .toList();
        return body(HttpStatus.BAD_REQUEST, "Hay errores de validación en la petición", errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraint(ConstraintViolationException ex) {
        List<String> errors = ex.getConstraintViolations().stream()
                .map(v -> v.getMessage())
                .sorted()
                .toList();
        return body(HttpStatus.BAD_REQUEST, "Parámetro inválido", errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException ex) {
        return body(HttpStatus.BAD_REQUEST, "El cuerpo de la petición no es un JSON válido o tiene tipos incorrectos", null);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<Map<String, Object>> handleInvalid(InvalidRequestException ex) {
        return body(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return body(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    /** Errores devueltos por RabbitMQ (o de conexión con el clúster). */
    @ExceptionHandler(AmqpException.class)
    public ResponseEntity<Map<String, Object>> handleAmqp(AmqpException ex) {
        String detail = rootMessage(ex);
        log.warn("Error de RabbitMQ: {}", detail);
        if (detail.contains("PRECONDITION_FAILED")) {
            return body(HttpStatus.CONFLICT,
                    "El recurso ya existe con otros parámetros (por ejemplo, otros argumentos). "
                            + "Elimínelo primero o use los mismos valores. Detalle: " + detail, null);
        }
        if (detail.contains("NOT_FOUND")) {
            return body(HttpStatus.NOT_FOUND,
                    "RabbitMQ indica que el recurso no existe. Detalle: " + detail, null);
        }
        return body(HttpStatus.SERVICE_UNAVAILABLE,
                "No se pudo completar la operación en RabbitMQ. Detalle: " + detail, null);
    }

    // Deja pasar los 404 de recursos estáticos (swagger, favicon) para que no se vuelvan 500
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResource(NoResourceFoundException ex) {
        return body(HttpStatus.NOT_FOUND, "Ruta no encontrada: " + ex.getResourcePath(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        log.error("Error inesperado", ex);
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", null);
    }

    private ResponseEntity<Map<String, Object>> body(HttpStatus status, String message, List<String> errors) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("timestamp", LocalDateTime.now().toString());
        map.put("status", status.value());
        map.put("error", status.getReasonPhrase());
        map.put("message", message);
        if (errors != null && !errors.isEmpty()) {
            map.put("errors", errors);
        }
        return ResponseEntity.status(status).body(map);
    }

    /** Junta los mensajes de toda la cadena de causas (RabbitMQ esconde el motivo real adentro). */
    public static String rootMessage(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        Throwable t = ex;
        int depth = 0;
        while (t != null && depth < 8) {
            if (t.getMessage() != null) {
                sb.append(t.getMessage()).append(" | ");
            }
            t = t.getCause();
            depth++;
        }
        return sb.toString();
    }
}
