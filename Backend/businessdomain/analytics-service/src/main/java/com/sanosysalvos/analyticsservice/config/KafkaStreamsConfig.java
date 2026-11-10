package com.sanosysalvos.analyticsservice.config;

import com.sanosysalvos.analyticsservice.streams.AnalyticsTopology;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.streams.StreamsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Activa Kafka Streams y arma la topología.
 * Los NewTopic aseguran que los topics existan antes de que parta el procesamiento
 * (la creación automática de topics está apagada en los brokers).
 */
@Configuration
@EnableKafkaStreams
public class KafkaStreamsConfig {

    private final KafkaAppProperties props;

    public KafkaStreamsConfig(KafkaAppProperties props) {
        this.props = props;
    }

    @Autowired
    public void topologia(StreamsBuilder builder) {
        AnalyticsTopology.construir(builder,
                props.getTopics().getReportEvents(),
                props.getTopics().getAlerts(),
                props.getAlertas());
    }

    @Bean
    public NewTopic topicEventos() {
        return crear(props.getTopics().getReportEvents(), props.getTopics().configDe("report-events"));
    }

    @Bean
    public NewTopic topicAlertas() {
        return crear(props.getTopics().getAlerts(), props.getTopics().configDe("alerts"));
    }

    private NewTopic crear(String nombre, KafkaAppProperties.TopicConfig c) {
        return TopicBuilder.name(nombre)
                .partitions(c.getPartitions())
                .replicas(c.getReplicas())
                .config("retention.ms", String.valueOf(c.getRetentionMs()))
                .config("cleanup.policy", c.getCleanupPolicy())
                .config("min.insync.replicas", String.valueOf(c.getMinInsyncReplicas()))
                .build();
    }
}
