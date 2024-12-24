package org.cyducks.satark.config;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.cyducks.generated.Report;
import org.cyducks.satark.serde.ReportSerde;
import org.cyducks.satark.serde.ReportSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
public class ReportProducerConfiguration {

    @Bean
    public ProducerFactory<String, Report> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfig());
    }

    @Bean
    public Map<String, Object> producerConfig() {
        Map<String, Object> properties = new HashMap<>();

        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-m:29093");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ReportSerializer.class);

        return properties;
    }

    @Bean
    public KafkaTemplate<String, Report> kafkaTemplate() {
        return new KafkaTemplate<String, Report>(producerFactory());
    }
}
