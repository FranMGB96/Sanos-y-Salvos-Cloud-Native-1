package com.sanosysalvos.rabbitadmin.controller;

import com.sanosysalvos.rabbitadmin.dto.QueueRequest;
import com.sanosysalvos.rabbitadmin.dto.ValidationPatterns;
import com.sanosysalvos.rabbitadmin.service.RabbitResourceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/queues")
@Validated
public class QueueController {

    private final RabbitResourceService service;

    public QueueController(RabbitResourceService service) {
        this.service = service;
    }

    /** POST /queues */
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody QueueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createQueue(request));
    }

    /** GET /queues/{name} -> mensajes y consumidores de la cola */
    @GetMapping("/{name}")
    public Map<String, Object> get(
            @PathVariable @Pattern(regexp = ValidationPatterns.RESOURCE_NAME, message = ValidationPatterns.NAME_MESSAGE) String name) {
        return service.getQueue(name);
    }

    /** DELETE /queues/{name} */
    @DeleteMapping("/{name}")
    public Map<String, Object> delete(
            @PathVariable @Pattern(regexp = ValidationPatterns.RESOURCE_NAME, message = ValidationPatterns.NAME_MESSAGE) String name) {
        return service.deleteQueue(name);
    }
}
