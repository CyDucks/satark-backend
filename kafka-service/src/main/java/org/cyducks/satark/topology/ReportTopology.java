package org.cyducks.satark.topology;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Suppressed;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.processor.WallclockTimestampExtractor;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.cyducks.generated.Report;
import org.cyducks.satark.serde.ReportArraySerde;
import org.cyducks.satark.serde.ReportSerde;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
public class ReportTopology {
    public static Topology buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        Serde<Report> reportSerde = new ReportSerde();
        Serde<List<Report>> reportListSerde = new ReportArraySerde();


        StoreBuilder<KeyValueStore<String, List<Report>>> reportStoreBuilder =
                Stores.keyValueStoreBuilder(
                        Stores.persistentKeyValueStore("report-store"),
                        Serdes.String(),
                        reportListSerde
                );

        StoreBuilder<KeyValueStore<String, Long>> windowStoreBuilder =
                Stores.keyValueStoreBuilder(
                        Stores.persistentKeyValueStore("window-store"),
                        Serdes.String(),
                        Serdes.Long()
                );

        builder.addStateStore(reportStoreBuilder);
        builder.addStateStore(windowStoreBuilder);

        List<Report> reports = new ArrayList<>();

        builder
                .stream("reports", Consumed.with(Serdes.String(), reportSerde)
                        .withTimestampExtractor(new WallclockTimestampExtractor()))
                .process(() -> new DynamicWindowProcessor(), "report-store", "window-store")
                .to("mass-reports", Produced.with(Serdes.String(), reportListSerde));


        return builder.build();

    }

    public static List<Report> getReportById(List<Report> reports, String id) {
        List<Report> matchingReports = new ArrayList<>();
        for (Report  report:
                reports) {
            if(report.getModeratorId().equals(id)) {
                matchingReports.add(report);
            }
        }

        return matchingReports;
    }

}
