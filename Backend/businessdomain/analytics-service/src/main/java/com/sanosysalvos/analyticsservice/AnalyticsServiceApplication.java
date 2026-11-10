package com.sanosysalvos.analyticsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/** analytics-service: estadísticas en vivo y alertas por zona con Kafka Streams (Evaluación 3). */
@SpringBootApplication
@ConfigurationPropertiesScan
public class AnalyticsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalyticsServiceApplication.class, args);
    }
}
