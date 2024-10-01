package org.cyducks.satark.serde;

import org.apache.kafka.common.serialization.Serializer;
import org.cyducks.generated.Report;

public class ReportSerializer implements Serializer<Report> {
    public ReportSerializer() {

    }

    @Override
    public byte[] serialize(String s, Report report) {
        return report.toByteArray();
    }
}
