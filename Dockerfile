# ── Stage 1: Build ──────────────────────────────────────────────────────────
FROM gradle:8.14-jdk21-alpine AS builder

WORKDIR /app

# Copy gradle config first (layer-cache friendly)
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle

# Download dependencies before copying source (cached unless build.gradle changes)
RUN ./gradlew dependencies --no-daemon || true

# Copy source and build the fat JAR, skipping tests
COPY src ./src
RUN ./gradlew bootJar -x test --no-daemon

# ── Stage 2: Run ────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /app/build/libs/star-group-vms-0.0.1-SNAPSHOT.jar app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
