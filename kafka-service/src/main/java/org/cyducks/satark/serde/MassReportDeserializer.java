package org.cyducks.satark.serde;

import org.apache.kafka.common.serialization.Deserializer;
import org.cyducks.generated.Report;

import java.util.List;

public class MassReportDeserializer implements Deserializer<List<Report>> {
    @Override
    public List<Report> deserialize(String s, byte[] bytes) {
        ReportArraySerde serde = new ReportArraySerde();

        return serde.deserializer().deserialize(s, bytes);
    }
}
