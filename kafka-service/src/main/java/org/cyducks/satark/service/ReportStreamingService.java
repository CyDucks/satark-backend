package org.cyducks.satark.service;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.cyducks.generated.Report;
import org.cyducks.generated.ReportRequest;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
@Slf4j
public class ReportStreamingService {
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<StreamObserver<Report>>> activeStreams = new ConcurrentHashMap<>();

    public void addReport(String key, Report report) {
        ConcurrentLinkedQueue<StreamObserver<Report>> observers = activeStreams.get(key);
        if(observers != null) {
            for(StreamObserver<Report> observer : observers) {
                observer.onNext(report);
                log.info("Sent report {} to client", report.getLocation());
            }
        }
    }

    @KafkaListener(topics = "reports", containerFactory = "reportListenerContainerFactory")
    public void processReports(Report report, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        log.info("report received");
        addReport(key, report);
    }

    public void streamReports(String key, StreamObserver<Report> reportStreamObserver) {
        log.info("streamReports Activated");
        activeStreams.computeIfAbsent(key, k -> new ConcurrentLinkedQueue<>()).add(reportStreamObserver);
    }

    public void removeStream(String key, StreamObserver<Report> observer) {
        ConcurrentLinkedQueue<StreamObserver<Report>> observers = activeStreams.get(key);
        if(observers != null) {
            observers.remove(observer);
            if(observers.isEmpty()) {
                activeStreams.remove(key);
            }
        }

    }

}
