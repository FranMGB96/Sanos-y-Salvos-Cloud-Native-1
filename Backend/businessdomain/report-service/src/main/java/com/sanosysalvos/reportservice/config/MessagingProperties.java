package com.sanosysalvos.reportservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Nombres de exchanges, colas y routing keys de RabbitMQ.
 * Se cargan desde el application.yml compartido del config-server
 * (prefijo "sanosysalvos.messaging"), así el código no escribe
 * ningún nombre a mano.
 */
@Data
@ConfigurationProperties(prefix = "sanosysalvos.messaging")
public class MessagingProperties {

    private Exchanges exchanges = new Exchanges();
    private Queues queues = new Queues();
    private RoutingKeys routingKeys = new RoutingKeys();

    @Data
    public static class Exchanges {
        private String reportsTopic;
        private String ticketsDirect;
        private String dlx;
    }

    @Data
    public static class Queues {
        private String reportCreated;
        private String reportCreatedDlq;
        private String ticket;
        private String ticketDlq;
        private String audit;
        private String auditDlq;
    }

    @Data
    public static class RoutingKeys {
        private String reportCreatedPrefix;
        private String reportCreatedPattern;
        private String ticketGenerate;
        private String statusPrefix;
        private String statusPattern;
        private String dlqReportCreated;
        private String dlqTicket;
        private String dlqAudit;
    }
}
