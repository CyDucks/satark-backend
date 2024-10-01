package org.cyducks.satark.serde;

import lombok.SneakyThrows;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.cyducks.generated.Report;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ReportArraySerde implements Serde<List<Report>> {

    @Override
    public Serializer<List<Report>> serializer() {
        return (topic, data) -> serialize(data);
    }

    @Override
    public Deserializer<List<Report>> deserializer() {
        return (topic, bytes) -> deserialize(bytes);
    }

    @SneakyThrows
    private byte[] serialize(List<Report> data) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        for(Report report : data) {
            byte[] serializedReport = report.toByteArray();

            bos.write(intToByteArray(serializedReport.length));
            bos.write(serializedReport);
        }

        return bos.toByteArray();
    }

    private static byte[] intToByteArray(int value) {
        return new byte[] {
                (byte) ((value >> 24) & 0xFF),
                (byte) ((value >> 16) & 0xFF),
                (byte) ((value >> 8) & 0xFF),
                (byte) (value & 0xFF)
        };
    }

    @SneakyThrows
    private List<Report> deserialize(byte[] bytes) {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);

        List<Report> reportList = new ArrayList<>();

        while(bis.available() > 0) {
            byte[] sizeBytes = new byte[4];

            bis.read(sizeBytes);
            int size = byteArrayToInt(sizeBytes);

            byte[] reportData = new byte[size];
            bis.read(reportData);

            Report report = Report.parseFrom(reportData);

            reportList.add(report);
        }

        return reportList;
    }

    private static int byteArrayToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8) |
                (bytes[3] & 0xFF);
    }
}
