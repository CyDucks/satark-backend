package org.cyducks.satark;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class GrpcServerConfig {

    @Bean
    public Server grpcServer(ReportServiceImpl reportService) throws IOException {
        Server server = ServerBuilder.forPort(9095)  // Define the port for gRPC
                .addService(reportService)         // Bind the service implementation
                .build()
                .start();

        System.out.println("gRPC Server started on port 9000");

        // Add shutdown hook for gracefully stopping the server
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));

        return server;
    }
}
