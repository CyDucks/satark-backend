package org.cyducks.satark.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.cyducks.generated.Report;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ReportProducerService {

    private static final ObjectMapper mapper = new ObjectMapper();
    private KafkaTemplate<String, Report> kafkaTemplate;

    public void sendReport(Report report) {
        kafkaTemplate.send("reports", report.getModeratorId(), report);
    }

    @SneakyThrows
    private String toJson(Report report) {
        return mapper.writeValueAsString(report);
    }
}
