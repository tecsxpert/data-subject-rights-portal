# ── Stage 1: Build ────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependencies before copying source
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Build (skip tests — run tests in CI separately)
COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true -q

# ── Stage 2: Runtime ──────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create upload directory
RUN mkdir -p /app/uploads/dsr

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
