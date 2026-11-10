package com.sanosysalvos.analyticsservice;

import com.sanosysalvos.analyticsservice.config.KafkaAppProperties;
import com.sanosysalvos.analyticsservice.model.AlertaEvent;
import com.sanosysalvos.analyticsservice.model.ReportEvent;
import com.sanosysalvos.analyticsservice.model.ResumenReportes;
import com.sanosysalvos.analyticsservice.streams.AnalyticsTopology;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba de la topología completa SIN brokers, con TopologyTestDriver de Kafka Streams.
 * Se mandan eventos al topic de entrada y se revisa lo que sale en el de alertas y en los state stores.
 */
class AnalyticsTopologyTest {

    private static final String EVENTOS = "sys.reports.events";
    private static final String ALERTAS = "sys.reports.alerts";

    private TopologyTestDriver driver;
    private TestInputTopic<String, ReportEvent> entrada;
    private TestOutputTopic<String, AlertaEvent> salida;
    private final Instant base = Instant.parse("2026-11-05T21:00:00Z");

    @BeforeEach
    void setUp() {
        StreamsBuilder builder = new StreamsBuilder();
        AnalyticsTopology.construir(builder, EVENTOS, ALERTAS, new KafkaAppProperties.Alertas());

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "analytics-test");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        props.put(StreamsConfig.STATESTORE_CACHE_MAX_BYTES_CONFIG, 0);
        driver = new TopologyTestDriver(builder.build(), props);

        JsonSerde<ReportEvent> eventoSerde = new JsonSerde<>(ReportEvent.class).ignoreTypeHeaders().noTypeInfo();
        JsonSerde<AlertaEvent> alertaSerde = new JsonSerde<>(AlertaEvent.class).ignoreTypeHeaders().noTypeInfo();
        entrada = driver.createInputTopic(EVENTOS, new StringSerializer(), eventoSerde.serializer());
        salida = driver.createOutputTopic(ALERTAS, new StringDeserializer(), alertaSerde.deserializer());
    }

    @AfterEach
    void tearDown() {
        driver.close();
    }

    private ReportEvent perdido(long reportId, double lat, double lon) {
        return ReportEvent.builder().eventId(UUID.randomUUID().toString()).tipoEvento(ReportEvent.REPORTE_CREADO)
                .reportId(reportId).tipo("PERDIDO").estado("ACTIVO").latitud(lat).longitud(lon)
                .timestamp(base.toString()).build();
    }

    private void enviar(ReportEvent e, int minutos) {
        entrada.pipeInput(String.valueOf(e.getReportId()), e, base.plusSeconds(minutos * 60L));
    }

    @Test
    @DisplayName("3 perdidos en la misma zona dentro de la hora -> 1 alerta MEDIA")
    void tresPerdidosMismaZona_generaAlertaMedia() {
        enviar(perdido(15, -33.4489, -70.6693), 1);
        enviar(perdido(16, -33.4512, -70.6701), 10);
        enviar(perdido(17, -33.4470, -70.6680), 20);

        List<AlertaEvent> alertas = salida.readValuesToList();
        assertEquals(1, alertas.size());
        AlertaEvent alerta = alertas.get(0);
        assertEquals("-33.45_-70.67", alerta.getZona());
        assertEquals(3, alerta.getCantidad());
        assertEquals("MEDIA", alerta.getSeveridad());
        assertEquals(List.of(15L, 16L, 17L), alerta.getReportIds());
    }

    @Test
    @DisplayName("Perdidos en zonas distintas -> no hay alerta")
    void zonasDistintas_noGeneraAlerta() {
        enviar(perdido(1, -33.45, -70.67), 1);
        enviar(perdido(2, -33.60, -70.57), 2);
        enviar(perdido(3, -33.02, -71.55), 3);
        assertTrue(salida.isEmpty());
    }

    @Test
    @DisplayName("5 perdidos en la misma zona -> alerta MEDIA (3) y luego ALTA (5)")
    void cincoPerdidos_escalaAAlta() {
        for (int i = 1; i <= 5; i++) {
            enviar(perdido(100 + i, -33.45, -70.67), i);
        }
        List<AlertaEvent> alertas = salida.readValuesToList();
        assertEquals(2, alertas.size());
        assertEquals("MEDIA", alertas.get(0).getSeveridad());
        assertEquals("ALTA", alertas.get(1).getSeveridad());
    }

    @Test
    @DisplayName("Los contadores se actualizan con creaciones y cambios de estado")
    void resumen_cuentaCreadosYCambiosDeEstado() {
        enviar(perdido(1, -33.45, -70.67), 1);
        entrada.pipeInput("2", ReportEvent.builder().eventId("e2").tipoEvento(ReportEvent.REPORTE_CREADO)
                .reportId(2L).tipo("ENCONTRADO").estado("ACTIVO").timestamp(base.toString()).build(),
                base.plusSeconds(120));
        entrada.pipeInput("1", ReportEvent.builder().eventId("e3").tipoEvento(ReportEvent.ESTADO_CAMBIADO)
                .reportId(1L).tipo("PERDIDO").estadoAnterior("ACTIVO").estado("RESUELTO")
                .timestamp(base.toString()).build(), base.plusSeconds(180));

        KeyValueStore<String, ResumenReportes> store = driver.getKeyValueStore(AnalyticsTopology.STORE_RESUMEN);
        ResumenReportes r = store.get(AnalyticsTopology.CLAVE_RESUMEN);
        assertEquals(2, r.getTotalReportes());
        assertEquals(1, r.getPerdidos());
        assertEquals(1, r.getEncontrados());
        assertEquals(1, r.getActivos());
        assertEquals(1, r.getResueltos());
    }

    @Test
    @DisplayName("La zona se calcula redondeando a 2 decimales")
    void zona_redondea() {
        assertEquals("-33.45_-70.67", AnalyticsTopology.zona(-33.4489, -70.6693));
    }
}
