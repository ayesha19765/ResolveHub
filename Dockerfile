# Stage 1: Build stage
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy project definition and resolve dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build production artifact
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Minimal runtime image
FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app

# Run as non-root user for container security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy packaged JAR from builder stage
COPY --from=builder --chown=spring:spring /app/target/resolvehub-0.0.1-SNAPSHOT.jar app.jar

# Application port
EXPOSE 8081

# Startup command
ENTRYPOINT ["java", "-jar", "app.jar"]
