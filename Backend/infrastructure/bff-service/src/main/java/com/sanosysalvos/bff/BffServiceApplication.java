package com.sanosysalvos.bff;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication @EnableDiscoveryClient
@OpenAPIDefinition(info = @Info(title = "BFF Service API", version = "1.0", description = "Backend For Frontend"))
public class BffServiceApplication {
    public static void main(String[] args) { SpringApplication.run(BffServiceApplication.class, args); }
}
