package com.sanosysalvos.kafkaadmin;

import com.sanosysalvos.kafkaadmin.dto.UpdateTopicConfigRequest;
import com.sanosysalvos.kafkaadmin.exception.InvalidRequestException;
import com.sanosysalvos.kafkaadmin.service.KafkaAdminService;
import org.apache.kafka.clients.admin.AdminClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

/** Reglas de la API de administración que se validan antes de llamar a Kafka. */
@ExtendWith(MockitoExtension.class)
class KafkaAdminServiceTest {

    @Mock private AdminClient adminClient;
    @InjectMocks private KafkaAdminService service;

    @Test
    @DisplayName("No se pueden eliminar los topics internos de Kafka")
    void eliminarTopicInterno_rechaza() {
        assertThrows(InvalidRequestException.class, () -> service.eliminarTopic("__consumer_offsets"));
        verifyNoInteractions(adminClient);
    }

    @Test
    @DisplayName("Modificar configuración sin ningún cambio -> error 400")
    void modificarSinCambios_rechaza() {
        assertThrows(InvalidRequestException.class,
                () -> service.modificarConfig("sys.reports.events", new UpdateTopicConfigRequest()));
        verifyNoInteractions(adminClient);
    }
}
