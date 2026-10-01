package com.sanosysalvos.rabbitadmin;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@OpenAPIDefinition(info = @Info(title = "RabbitMQ Admin API", version = "1.0",
        description = "Crear y eliminar colas, exchanges y bindings de RabbitMQ"))
public class RabbitAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(RabbitAdminApplication.class, args);
    }
}
