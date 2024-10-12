package org.cyducks.satark;

import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.cyducks.generated.Report;
import org.cyducks.generated.ReportAck;
import org.cyducks.generated.ReportRequest;
import org.cyducks.generated.ReportServiceGrpc;
import org.cyducks.satark.service.ReportProducerService;
import org.cyducks.satark.service.ReportStreamingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

@GrpcService
@Slf4j
public class ReportServiceImpl extends ReportServiceGrpc.ReportServiceImplBase {

    @Autowired
    private ReportProducerService producerService;

    @Autowired
    private ReportStreamingService streamingService;

    @Override
    public void sendReport(Report request, StreamObserver<ReportAck> responseObserver) {
        // TODO: forward report to the appropriate kafka topic partition
        producerService.sendReport(request);

        log.info("sendReport in ReportServiceImpl");
        ReportAck ack = ReportAck.newBuilder().setStatus(200).build();
        responseObserver.onNext(ack);
        responseObserver.onCompleted();
    }

    @Override
    public void getLiveReports(ReportRequest request, StreamObserver<Report> responseObserver) {
        ServerCallStreamObserver<Report> observer = (ServerCallStreamObserver<Report>) responseObserver;

        observer.setOnCancelHandler(() -> {
            streamingService.removeStream(request.getModeratorId(), observer);
        });

        streamingService.streamReports(request.getModeratorId(), responseObserver);
    }
}
