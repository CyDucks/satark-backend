package org.cyducks.satark.service;

import lombok.extern.slf4j.Slf4j;
import org.cyducks.generated.Report;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;

@Service
@Slf4j
public class MassReportConsumerService {

    @Autowired
    private FCMService fcmService;

    @Autowired
    private ReportStreamingService reportStreamingService;

    @KafkaListener(topics = "mass-reports", containerFactory = "kafkaListenerContainerFactory")
    public void getReports(List<Report> reports, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        log.info("MASS REPORT RECEIVED");


        try {
            log.info(key);
            // Create a list of locations from the reports
            List<Map<String, Float>> locations = reports.stream()
                    .map(report -> {
                        Map<String, Float> location = new HashMap<>();
                        location.put("lat", report.getLocation().getLat());
                        location.put("lng", report.getLocation().getLng());
                        return location;
                    })
                    .collect(Collectors.toList());

            // Convert locations to JSON string
            ObjectMapper mapper = new ObjectMapper();
            String locationData = mapper.writeValueAsString(locations);
            fcmService.sendPushNotification(
                    key,
                    "Mass Report Detected",
                    "Multiple reports detected in your zone, please review.",
                    Map.of("locations", locationData)
            );

            for(Report report : reports) {
                reportStreamingService.addReport(key, report);
            }

        } catch (Exception e) {
            log.error(e.toString());
        }


        for(Report report : reports) {
            log.info(report.toString());
        }
    }
}
