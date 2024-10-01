package org.cyducks.satark.topology;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Suppressed;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.cyducks.generated.Report;
import org.cyducks.satark.serde.ReportArraySerde;
import org.cyducks.satark.serde.ReportSerde;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ReportTopology {
    public static Topology buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        Serde<Report> reportSerde = new ReportSerde();
        Serde<List<Report>> reportListSerde = new ReportArraySerde();

        List<Report> reports = new ArrayList<>();

        builder
                .stream("reports", Consumed.with(Serdes.String(), reportSerde))
                .groupByKey()
                .windowedBy(TimeWindows.ofSizeAndGrace(Duration.ofSeconds(20L), Duration.ofSeconds(2L)))
                .aggregate(() -> 0L, (key, value, aggregate) -> {
                    reports.add(value);
                    return aggregate+1;
                })
                .suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded()))
                .toStream()
                .peek((key, value) -> log.info(String.format("{%s}:{%s}", key.key(), value)))
                .map((key, value) -> KeyValue.pair(key.key(), value))
                .filter((key, count) -> count >= 10L)
                .mapValues((readOnlyKey, value) -> getReportById(reports, readOnlyKey))
                .peek((key, value) -> reports.clear())
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
