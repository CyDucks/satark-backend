package org.cyducks.satark.topology;


import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.cyducks.generated.Report;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class DynamicWindowProcessor implements Processor<String, Report, String, List<Report>> {
    private ProcessorContext<String, List<Report>> processorContext;
    private KeyValueStore<String, List<Report>> reportStore;
    private KeyValueStore<String, Long> windowStore;


    @Override
    public void init(ProcessorContext<String, List<Report>> context) {
        this.processorContext = context;
        this.reportStore = context.getStateStore("report-store");
        this.windowStore = context.getStateStore("window-store");

        context.schedule(Duration.ofSeconds(5), PunctuationType.WALL_CLOCK_TIME, this::punctuate);

    }

    private void punctuate(long timestamp) {
        windowStore.all().forEachRemaining(entry -> {
            String key = entry.key;

            long windowStart = entry.value;

            if(timestamp - windowStart >= 20000) {
                List<Report> reports = reportStore.get(key);
                if(reports != null && !reports.isEmpty() && reports.size() >= 10) {
                    emitResult(key, reports);
                }
            } else {
                reportStore.delete(key);
                windowStore.delete(key);
            }
        });
    }

    private void emitResult(String key, List<Report> reports) {
        processorContext.forward(new Record<>(key, reports, System.currentTimeMillis()));
        reportStore.delete(key);
        windowStore.delete(key);
    }

    @Override
    public void process(Record<String, Report> record) {
        String key = record.key();
        Report report = record.value();

        List<Report> reports = reportStore.get(key);
        if(reports == null) {
            reports = new ArrayList<>();
            windowStore.put(key, System.currentTimeMillis());
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
