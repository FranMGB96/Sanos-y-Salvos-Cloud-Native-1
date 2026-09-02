package com.sanosysalvos.reportservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication @EnableDiscoveryClient
@OpenAPIDefinition(info = @Info(title = "Report Service API", version = "1.0"))
public class ReportServiceApplication {
    public static void main(String[] args) { SpringApplication.run(ReportServiceApplication.class, args); }
}
