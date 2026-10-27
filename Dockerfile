# ==============================================================================
# Multi-Stage Production Dockerfile for Bookworm Backend Platform
# Base: Red Hat Universal Base Image (UBI 9) OpenJDK 21
# Security: Non-Root Execution (UID 1001), Minimal Surface, Health Probes
# ==============================================================================

# ------------------------------------------------------------------------------
# Stage 1: Build Application with Maven & OpenJDK 21
# ------------------------------------------------------------------------------
FROM registry.access.redhat.com/ubi9/openjdk-21:latest AS builder

USER root
WORKDIR /build

# Copy Maven POM and dependency definitions first for optimal layer caching
COPY pom.xml .

# Download dependencies in offline-ready state
RUN mvn dependency:go-offline -B

# Copy application source code and resources
COPY src ./src

# Build production Spring Boot executable JAR skipping tests (tests run in CI/CD)
RUN mvn clean package -DskipTests -B

# ------------------------------------------------------------------------------
# Stage 2: Minimal Production Runtime
# ------------------------------------------------------------------------------
FROM registry.access.redhat.com/ubi9/openjdk-21-runtime:latest AS runtime

# Metadata labels
LABEL maintainer="Bookworm Engineering Team <dev@bookworm.example.com>"
LABEL version="1.0.0"
LABEL description="Enterprise E-Bookstore Platform Backend"

USER root
WORKDIR /app

# Install curl for container health check probes
RUN microdnf update -y && \
    microdnf install -y curl && \
    microdnf clean all && \
    rm -rf /var/cache/yum

# Create non-root dedicated application user & group
RUN groupadd -g 1001 appgroup && \
    useradd -u 1001 -g appgroup -m -s /bin/bash appuser

# Copy built artifact from builder stage with strict ownership
COPY --from=builder --chown=appuser:appgroup /build/target/bookworm-backend-1.0.0-SNAPSHOT.jar /app/app.jar

# Enforce secure read/execute permissions
RUN chmod 755 /app/app.jar

# Switch to non-root user
USER 1001

# Expose HTTP application port
EXPOSE 8080

# Production JVM Performance & Container Memory Tuning
ENV JAVA_OPTS="-XX:+UseG1GC \
               -XX:MaxRAMPercentage=75.0 \
               -XX:InitialRAMPercentage=50.0 \
               -XX:+ExitOnOutOfMemoryError \
               -Djava.security.egd=file:/dev/./urandom \
               -Dfile.encoding=UTF-8"

# Integrated Docker Health Check via Spring Boot Actuator
HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
    CMD curl -f http://127.0.0.1:8080/actuator/health || exit 1

# Launch Application Entrypoint
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
