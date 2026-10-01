package com.sanosysalvos.rabbitadmin.controller;

import com.sanosysalvos.rabbitadmin.dto.BindingRequest;
import com.sanosysalvos.rabbitadmin.service.RabbitResourceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/bindings")
public class BindingController {

    private final RabbitResourceService service;

    public BindingController(RabbitResourceService service) {
        this.service = service;
    }

    /** POST /bindings */
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody BindingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createBinding(request));
    }

    /** DELETE /bindings (los datos del binding van en el cuerpo JSON) */
    @DeleteMapping
    public Map<String, Object> delete(@Valid @RequestBody BindingRequest request) {
        return service.deleteBinding(request);
    }
}
