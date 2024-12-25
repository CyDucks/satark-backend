### Dockerfile ###
# Build stage
FROM maven:3.8-openjdk-17 AS builder

# Set working directory
WORKDIR /app

# Copy only the POM files first to cache dependencies
COPY pom.xml .
COPY application/pom.xml application/
COPY contract/pom.xml contract/
COPY firebase-service/pom.xml firebase-service/
COPY grpc-service/pom.xml grpc-service/
COPY kafka-service/pom.xml kafka-service/
COPY zone-service/pom.xml zone-service/

# Download dependencies
RUN mvn dependency:go-offline -B


# Copy source code
COPY . .

# Build the application
RUN mvn clean package -DskipTests
RUN ls -l application/target/


# Runtime stage
FROM openjdk:17-slim

WORKDIR /app

RUN apt-get update && apt-get install -y curl netcat && apt-get clean

# Create directory for external config
RUN mkdir -p /app/config

# Copy the built artifact from builder stage
COPY --from=builder /app/application/target/*.jar app.jar

# Copy the entrypoint script
COPY docker-entrypoint.sh /app/
RUN chmod +x /app/docker-entrypoint.sh

EXPOSE 8080
EXPOSE 9095

RUN sed -i 's/\r$//' /app/docker-entrypoint.sh

ENTRYPOINT ["/app/docker-entrypoint.sh"]
