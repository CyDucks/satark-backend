package org.cyducks.satark;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.cyducks.generated.Report;
import org.cyducks.generated.ReportAck;
import org.cyducks.generated.ReportServiceGrpc;
import org.cyducks.satark.service.ReportProducerService;
import org.springframework.beans.factory.annotation.Autowired;

import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

@GrpcService
@Slf4j
public class ReportServiceImpl extends ReportServiceGrpc.ReportServiceImplBase {

    @Autowired
    private ReportProducerService producerService;

    @Override
    public void sendReport(Report request, StreamObserver<ReportAck> responseObserver) {
        // TODO: forward report to the appropriate kafka topic partition
        producerService.sendReport(request);

        log.info("sendReport in ReportServiceImpl");
        ReportAck ack = ReportAck.newBuilder().setStatus(200).build();
        responseObserver.onNext(ack);
        responseObserver.onCompleted();
    }
}
