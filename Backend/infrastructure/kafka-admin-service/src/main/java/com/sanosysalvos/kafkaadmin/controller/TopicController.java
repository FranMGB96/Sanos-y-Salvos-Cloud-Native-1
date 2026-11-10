package com.sanosysalvos.kafkaadmin.controller;

import com.sanosysalvos.kafkaadmin.dto.CreateTopicRequest;
import com.sanosysalvos.kafkaadmin.dto.IncreasePartitionsRequest;
import com.sanosysalvos.kafkaadmin.dto.TopicDetail;
import com.sanosysalvos.kafkaadmin.dto.TopicSummary;
import com.sanosysalvos.kafkaadmin.dto.UpdateTopicConfigRequest;
import com.sanosysalvos.kafkaadmin.dto.ValidationPatterns;
import com.sanosysalvos.kafkaadmin.service.KafkaAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/kafka/topics")
@Validated
@Tag(name = "Topics", description = "Crear, listar, modificar y eliminar topics y sus configuraciones")
public class TopicController {

    private final KafkaAdminService service;

    public TopicController(KafkaAdminService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar topics (por defecto sin los internos de Kafka)")
    public List<TopicSummary> listar(@RequestParam(defaultValue = "false") boolean internos) {
        return service.listarTopics(internos);
    }

    @GetMapping("/{nombre}")
    @Operation(summary = "Detalle: particiones (líder, réplicas, ISR, offsets) y configuración")
    public TopicDetail detalle(@PathVariable @Pattern(regexp = ValidationPatterns.TOPIC_NAME) String nombre) {
        return service.detalleTopic(nombre);
    }

    @PostMapping
    @Operation(summary = "Crear un topic con particiones, réplicas, retención y cleanup policy")
    public ResponseEntity<TopicDetail> crear(@Valid @RequestBody CreateTopicRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearTopic(req));
    }

    @PutMapping("/{nombre}/config")
    @Operation(summary = "Modificar configuración (retention.ms, cleanup.policy, min.insync.replicas)")
    public TopicDetail modificarConfig(@PathVariable @Pattern(regexp = ValidationPatterns.TOPIC_NAME) String nombre,
                                       @Valid @RequestBody UpdateTopicConfigRequest req) {
        return service.modificarConfig(nombre, req);
    }

    @PutMapping("/{nombre}/partitions")
    @Operation(summary = "Aumentar particiones (Kafka no permite disminuirlas)")
    public TopicDetail aumentarParticiones(@PathVariable @Pattern(regexp = ValidationPatterns.TOPIC_NAME) String nombre,
                                           @Valid @RequestBody IncreasePartitionsRequest req) {
        return service.aumentarParticiones(nombre, req.getTotalPartitions());
    }

    @DeleteMapping("/{nombre}")
    @Operation(summary = "Eliminar un topic")
    public ResponseEntity<Void> eliminar(@PathVariable @Pattern(regexp = ValidationPatterns.TOPIC_NAME) String nombre) {
        service.eliminarTopic(nombre);
        return ResponseEntity.noContent().build();
    }
}
