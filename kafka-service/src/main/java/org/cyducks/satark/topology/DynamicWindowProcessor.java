package org.cyducks.satark.topology;


import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.cyducks.generated.Report;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

@Slf4j
public class DynamicWindowProcessor implements Processor<String, Report, String, List<Report>> {

    private static final int WINDOW_SIZE = 20000;


    private ProcessorContext<String, List<Report>> processorContext;
    private KeyValueStore<String, List<Report>> reportStore;
    private KeyValueStore<String, Long> windowStore;
    private PriorityBlockingQueue<WindowExpiration> expirationQueue;


    @Override
    public void init(ProcessorContext<String, List<Report>> context) {
        this.processorContext = context;
        this.reportStore = context.getStateStore("report-store");
        this.windowStore = context.getStateStore("window-store");
        this.expirationQueue = new PriorityBlockingQueue<>();

        context.schedule(Duration.ofSeconds(5), PunctuationType.WALL_CLOCK_TIME, this::punctuate);

    }

    private void punctuate(long timestamp) {
        WindowExpiration expiration = expirationQueue.peek();

        while(expiration != null && expiration.getExpirationTime() <= timestamp) {
            expirationQueue.poll();
            String key = expiration.getKey();

            log.info("Window expired at: " + new Date(timestamp));

            List<Report> reports = reportStore.get(key);
            if(reports != null && reports.size() >= 10) {
                emitResult(key, reports);
            } else {
                reportStore.delete(key);
                windowStore.delete(key);
            }
            expiration = expirationQueue.peek();
        }
    }

    private void emitResult(String key, List<Report> reports) {
        processorContext.forward(new Record<>(key, reports, processorContext.currentSystemTimeMs()));
        reportStore.delete(key);
        windowStore.delete(key);
    }

    @Override
    public void process(Record<String, Report> record) {
        String key = record.key();
        Report report = record.value();

        long timestamp = processorContext.currentSystemTimeMs();


        Long windowStart = windowStore.get(key);

        if(windowStart == null  || timestamp > windowStart + WINDOW_SIZE) {
            windowStart = timestamp;
            windowStore.put(key, windowStart);
            expirationQueue.offer(new WindowExpiration(key, windowStart + WINDOW_SIZE));
            log.info("Expiration added at: " + new Date(windowStart));
        }

        List<Report> reports = reportStore.get(key);
        if(reports == null) {
            reports = new ArrayList<>();
        }

        reports.add(report);
        reportStore.put(key, reports);

        if(reports.size() >= 10) {
            emitResult(key, reports);
        }
    }

    @Override
    public void close() {
        Processor.super.close();
    }
}
