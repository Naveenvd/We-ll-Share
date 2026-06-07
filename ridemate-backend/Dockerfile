# ─────────────────────────────────────────────────────
# Stage 1: Build the Spring Boot app with Maven
# ─────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Copy pom.xml first (Docker layer caching — only re-downloads
# dependencies if pom.xml changes, not on every code change)
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source code and build (skip tests — Jenkins runs them separately)
COPY src ./src
RUN mvn clean package -DskipTests -q

# ─────────────────────────────────────────────────────
# Stage 2: Run with lightweight JRE (not full JDK)
# ─────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create uploads directory for user files
RUN mkdir -p /app/uploads

# Copy the built JAR from stage 1
COPY --from=builder /app/target/ridemate-backend-1.0.0.jar app.jar

# Expose the Spring Boot port
EXPOSE 8081

# Run the app
# application.properties is passed via Kubernetes secrets/configmap at runtime
ENTRYPOINT ["java", "-jar", "app.jar"]
