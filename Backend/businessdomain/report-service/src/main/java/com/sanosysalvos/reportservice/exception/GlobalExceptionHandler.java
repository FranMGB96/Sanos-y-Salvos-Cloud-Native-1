package com.sanosysalvos.reportservice.exception;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import java.time.LocalDateTime;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String,Object>> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst().orElse("Error de validación");
        return build(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<String> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,Object>> handleGeneral(Exception ex) throws NoResourceFoundException {
        // ✅ Dejar que Spring maneje rutas no encontradas (actuator, etc.)
        if (ex instanceof NoResourceFoundException nrfe) {
            throw nrfe;
        }
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error: " + ex.getMessage());
    }

    private ResponseEntity<Map<String,Object>> build(HttpStatus s, String msg) {
        Map<String,Object> b = new HashMap<>();
        b.put("timestamp", LocalDateTime.now().toString());
        b.put("status", s.value());
        b.put("error", s.getReasonPhrase());
        b.put("message", msg);
        return ResponseEntity.status(s).body(b);
    }
}