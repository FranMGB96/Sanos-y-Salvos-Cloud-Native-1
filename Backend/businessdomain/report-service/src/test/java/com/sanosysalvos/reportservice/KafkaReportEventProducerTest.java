package com.sanosysalvos.reportservice;

import com.sanosysalvos.reportservice.messaging.kafka.KafkaReportEventProducer;
import com.sanosysalvos.reportservice.messaging.kafka.ReportEvent;
import com.sanosysalvos.reportservice.model.Report;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Pruebas unitarias del productor de Kafka (sin brokers: el KafkaTemplate es un mock). */
@ExtendWith(MockitoExtension.class)
class KafkaReportEventProducerTest {

    @Mock private KafkaTemplate<String, ReportEvent> kafkaTemplate;
    private KafkaReportEventProducer producer;

    @BeforeEach
    void setUp() {
        producer = new KafkaReportEventProducer(kafkaTemplate, "sys.reports.events", true);
    }

    private Report reporte(Report.EstadoReporte estado) {
        return Report.builder().id(17L).tipo(Report.TipoReporte.PERDIDO).descripcion("Perro perdido")
                .latitud(-33.4489).longitud(-70.6693).petId(5L).reporterUserId(1L).estado(estado).build();
    }

    @Test
    @DisplayName("Reporte creado -> se publica en sys.reports.events con key = reportId")
    @SuppressWarnings("unchecked")
    void reporteCreado_publicaConKeyReportId() {
        CompletableFuture<SendResult<String, ReportEvent>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);

        producer.publishReportCreated(reporte(Report.EstadoReporte.ACTIVO));

        ArgumentCaptor<ProducerRecord<String, ReportEvent>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(captor.capture());
        ProducerRecord<String, ReportEvent> record = captor.getValue();
        assertEquals("sys.reports.events", record.topic());
        assertEquals("17", record.key());
        assertEquals(ReportEvent.REPORTE_CREADO, record.value().getTipoEvento());
        assertNull(record.value().getEstadoAnterior());
        assertEquals("ACTIVO", record.value().getEstado());
        assertNotNull(record.value().getEventId());
        assertNotNull(record.headers().lastHeader(KafkaReportEventProducer.CORRELATION_HEADER));
    }

    @Test
    @DisplayName("Cambio de estado real -> se publica con estado anterior y nuevo")
    @SuppressWarnings("unchecked")
    void cambioDeEstado_publicaEstadoAnterior() {
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(new CompletableFuture<>());

        producer.publishStatusChanged(reporte(Report.EstadoReporte.RESUELTO), Report.EstadoReporte.ACTIVO);

        ArgumentCaptor<ProducerRecord<String, ReportEvent>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(captor.capture());
        assertEquals(ReportEvent.ESTADO_CAMBIADO, captor.getValue().value().getTipoEvento());
        assertEquals("ACTIVO", captor.getValue().value().getEstadoAnterior());
        assertEquals("RESUELTO", captor.getValue().value().getEstado());
    }

    @Test
    @DisplayName("Si el estado no cambió, no se publica nada")
    void mismoEstado_noPublica() {
        producer.publishStatusChanged(reporte(Report.EstadoReporte.ACTIVO), Report.EstadoReporte.ACTIVO);
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    @DisplayName("Si Kafka falla al enviar, el error no sale del productor")
    @SuppressWarnings("unchecked")
    void kafkaCaido_noLanzaExcepcion() {
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenThrow(new RuntimeException("broker caído"));
        assertDoesNotThrow(() -> producer.publishReportCreated(reporte(Report.EstadoReporte.ACTIVO)));
    }
}
