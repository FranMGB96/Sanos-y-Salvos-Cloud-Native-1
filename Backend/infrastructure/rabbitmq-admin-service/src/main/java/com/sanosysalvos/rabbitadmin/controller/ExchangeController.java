package com.sanosysalvos.rabbitadmin.controller;

import com.sanosysalvos.rabbitadmin.dto.ExchangeRequest;
import com.sanosysalvos.rabbitadmin.dto.ValidationPatterns;
import com.sanosysalvos.rabbitadmin.service.RabbitResourceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/exchanges")
@Validated
public class ExchangeController {

    private final RabbitResourceService service;

    public ExchangeController(RabbitResourceService service) {
        this.service = service;
    }

    /** POST /exchanges */
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody ExchangeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createExchange(request));
    }

    /** DELETE /exchanges/{name} */
    @DeleteMapping("/{name}")
    public Map<String, Object> delete(
            @PathVariable @Pattern(regexp = ValidationPatterns.RESOURCE_NAME, message = ValidationPatterns.NAME_MESSAGE) String name) {
        return service.deleteExchange(name);
    }
}
