package com.sanosysalvos.petservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanosysalvos.petservice.messaging.kafka.KafkaEventParser;
import com.sanosysalvos.petservice.messaging.kafka.ReportEvent;
import com.sanosysalvos.petservice.messaging.support.InvalidMessageException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas de la conversión y validación de los mensajes que llegan por Kafka. */
class KafkaEventParserTest {

    private KafkaEventParser parser;

    @BeforeEach
    void setUp() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        ObjectMapper mapper = new ObjectMapper()
                .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        parser = new KafkaEventParser(mapper, validator);
    }

    @Test
    @DisplayName("Evento completo -> se convierte sin errores")
    void eventoValido_seConvierte() {
        String json = "{\"eventId\":\"e1\",\"tipoEvento\":\"REPORTE_CREADO\",\"reportId\":15,\"petId\":1,"
                + "\"tipo\":\"PERDIDO\",\"estado\":\"ACTIVO\",\"timestamp\":\"2026-11-06T00:22:11Z\"}";
        ReportEvent e = parser.parse(json, ReportEvent.class);
        assertEquals(15L, e.getReportId());
        assertEquals("REPORTE_CREADO", e.getTipoEvento());
    }

    @Test
    @DisplayName("Texto que no es JSON (hola) -> mensaje inválido")
    void noEsJson_esInvalido() {
        InvalidMessageException ex = assertThrows(InvalidMessageException.class,
                () -> parser.parse("hola", ReportEvent.class));
        assertTrue(ex.getMessage().contains("no es un JSON válido"));
    }

    @Test
    @DisplayName("JSON sin los campos obligatorios -> mensaje inválido con la lista de campos")
    void faltanCampos_esInvalido() {
        InvalidMessageException ex = assertThrows(InvalidMessageException.class,
                () -> parser.parse("{\"esto\":\"no es un evento\"}", ReportEvent.class));
        assertTrue(ex.getMessage().contains("eventId"));
        assertTrue(ex.getMessage().contains("reportId"));
    }
}
