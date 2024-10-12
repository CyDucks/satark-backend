package org.cyducks.satark.serde;

import org.apache.kafka.common.serialization.Deserializer;
import org.cyducks.generated.Report;

public class ReportDeserializer implements Deserializer<Report> {
    @Override
    public Report deserialize(String s, byte[] bytes) {
        try(ReportSerde serde = new ReportSerde()) {
            return serde.deserializer().deserialize(s, bytes);
        }
    }
}
