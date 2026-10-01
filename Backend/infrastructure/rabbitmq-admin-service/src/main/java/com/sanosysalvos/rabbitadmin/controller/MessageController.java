package com.sanosysalvos.rabbitadmin.controller;

import com.sanosysalvos.rabbitadmin.dto.PublishRequest;
import com.sanosysalvos.rabbitadmin.service.RabbitResourceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Utilidad de demostración: publicar un mensaje de prueba en un exchange. */
@RestController
@RequestMapping("/messages")
public class MessageController {

    private final RabbitResourceService service;

    public MessageController(RabbitResourceService service) {
        this.service = service;
    }

    /** POST /messages */
    @PostMapping
    public Map<String, Object> publish(@Valid @RequestBody PublishRequest request) {
        return service.publish(request);
    }
}
