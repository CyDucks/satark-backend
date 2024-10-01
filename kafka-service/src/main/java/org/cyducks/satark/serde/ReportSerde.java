package org.cyducks.satark.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.GeneratedMessageV3;
import lombok.SneakyThrows;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.cyducks.generated.Report;

public class ReportSerde implements Serde<Report> {

    public ReportSerde() {

    }

    @Override
    public Serializer<Report> serializer() {
        return (topic, data) -> serialize(data);
    }

    @Override
    public Deserializer<Report> deserializer() {
        return (topic, bytes) -> deserialize(bytes);
    }

    @SneakyThrows
    private byte[] serialize(Report data) {
        return data.toByteArray();
    }

    @SneakyThrows
    private Report deserialize(byte[] bytes) {
        return Report.parseFrom(bytes);
    }
}
