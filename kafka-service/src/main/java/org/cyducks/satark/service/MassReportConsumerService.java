package org.cyducks.satark.service;

import lombok.extern.slf4j.Slf4j;
import org.cyducks.generated.Report;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

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
            fcmService.sendPushNotification(
                    key,
                    "Mass Report Detected",
                    "Multiple reports detected in your zone, please review."
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
