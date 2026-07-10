# ============================================================
# AquaTrack Backend — Multi-Stage Dockerfile
# ============================================================
# Stage 1 (builder): Compile and package the application
# Stage 2 (runtime): Minimal JRE image with the fat JAR
# ============================================================

# ── Stage 1: Build ───────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

LABEL maintainer="AquaTrack Engineering <engineering@aquatrack.com>"
LABEL stage="builder"

WORKDIR /app

# Copy Maven wrapper and POM first (layer-cache dependency downloads)
COPY mvnw mvnw.cmd* ./
COPY .mvn/ .mvn/
COPY pom.xml ./

# Download dependencies (cached unless pom.xml changes)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B --no-transfer-progress

# Copy source code and build
COPY src/ src/
RUN ./mvnw package -DskipTests -B --no-transfer-progress

# ── Stage 2: Runtime ─────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

LABEL maintainer="AquaTrack Engineering <engineering@aquatrack.com>"
LABEL application="aquatrack-backend"
LABEL version="1.0.0"

# Security: run as non-root user
RUN addgroup -S aquatrack && adduser -S aquatrack -G aquatrack

WORKDIR /app

# Create log directory with correct ownership
RUN mkdir -p /app/logs && chown -R aquatrack:aquatrack /app

# Copy the fat JAR from the builder stage
COPY --from=builder --chown=aquatrack:aquatrack \
     /app/target/aquatrack-backend-*.jar app.jar

# Switch to non-root user
USER aquatrack

# Expose the application port
EXPOSE 8080

# JVM tuning for containerised environments:
# -XX:+UseContainerSupport        — respect container CPU/memory limits
# -XX:MaxRAMPercentage=75.0       — use up to 75% of container RAM for heap
# -XX:+UseG1GC                    — G1 garbage collector (default in Java 21)
# -Djava.security.egd             — faster SecureRandom on Linux
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod}", \
  "-jar", "app.jar"]

# Health check — polls Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1
