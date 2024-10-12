package org.cyducks.satark.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.cyducks.generated.Report;
import org.cyducks.satark.serde.MassReportDeserializer;
import org.cyducks.satark.serde.ReportDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableKafka
public class ReportConsumerConfiguration {

    @Bean
    public KafkaListenerContainerFactory<?>
        kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, List<Report>> containerFactory =
                new ConcurrentKafkaListenerContainerFactory<>();

        containerFactory.setConsumerFactory(massReportConsumerFactory());
        containerFactory.setConcurrency(3);
        containerFactory.getContainerProperties().setPollTimeout(3000);

        return containerFactory;

    }

    @Bean
    public KafkaListenerContainerFactory<?>
        reportListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, Report> containerFactory =
                new ConcurrentKafkaListenerContainerFactory<>();

        containerFactory.setConsumerFactory(reportConsumerFactory());
        containerFactory.setConcurrency(3);
        containerFactory.getContainerProperties().setPollTimeout(3000);

        return containerFactory;

    }

    @Bean
    public ConsumerFactory<String, Report> reportConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs(), new StringDeserializer(), new ReportDeserializer());
    }


    @Bean
    public ConsumerFactory<String, List<Report>> massReportConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs(), new StringDeserializer(), new MassReportDeserializer());
    }

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> configs = new HashMap<>();

        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29093");
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, "MyGroup");
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        return configs;
    }
}
