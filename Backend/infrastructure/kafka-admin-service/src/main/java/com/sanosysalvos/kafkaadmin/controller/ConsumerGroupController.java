package com.sanosysalvos.kafkaadmin.controller;

import com.sanosysalvos.kafkaadmin.dto.ConsumerGroupDetail;
import com.sanosysalvos.kafkaadmin.dto.ConsumerGroupSummary;
import com.sanosysalvos.kafkaadmin.service.KafkaAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/kafka")
@Tag(name = "Consumer groups y clúster", description = "Inspeccionar grupos, offsets, lag y brokers")
public class ConsumerGroupController {

    private final KafkaAdminService service;

    public ConsumerGroupController(KafkaAdminService service) {
        this.service = service;
    }

    @GetMapping("/consumer-groups")
    @Operation(summary = "Listar consumer groups y su estado")
    public List<ConsumerGroupSummary> listar() {
        return service.listarConsumerGroups();
    }

    @GetMapping("/consumer-groups/{groupId}")
    @Operation(summary = "Miembros, partición asignada a cada uno, offsets y lag")
    public ConsumerGroupDetail detalle(@PathVariable String groupId) {
        return service.detalleConsumerGroup(groupId);
    }

    @GetMapping("/cluster")
    @Operation(summary = "Brokers del clúster y cuál es el controlador")
    public Map<String, Object> cluster() {
        return service.infoCluster();
    }
}
