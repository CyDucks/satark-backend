package org.cyducks.satark.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.HostInfo;
import org.cyducks.satark.topology.ReportTopology;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Slf4j
@Configuration
public class ReportStreamConfiguration {

    @Value("${host.info:localhost:8080}")
    private String hostInfo;

    @Value("${kafka.streams.state.dir:/tmp/kafka-streams}")
    private String kafkaStreamsStateDir;

    @Bean
    public Properties kafkaStreamsConfiguration() {
        Properties properties = new Properties();

        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "civilian-reports");
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29093");
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Long().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.Long().getClass());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0");
        properties.put(StreamsConfig.APPLICATION_SERVER_CONFIG, hostInfo);
        properties.put(StreamsConfig.STATE_DIR_CONFIG, kafkaStreamsStateDir);

        return properties;
    }


    public Topology reportTopology() {
        return ReportTopology.buildTopology();
    }


    @Bean
    public KafkaStreams kafkaStreams(@Qualifier("kafkaStreamsConfiguration") Properties properties) {

        KafkaStreams streams = new KafkaStreams(reportTopology(), properties);

        streams.cleanUp();
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

        return streams;
    }

    @Bean
    public HostInfo hostInfo() {
        log.info("Creating host info: {}", hostInfo);
        var split = hostInfo.split(":");

        return new HostInfo(split[0], Integer.parseInt(split[1]));
    }
}
