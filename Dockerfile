# syntax=docker/dockerfile:1.7

############################################################
# Stage 1 — build Maven (cache-friendly)
############################################################
FROM maven:3.9.9-eclipse-temurin-17-alpine AS build
WORKDIR /workspace

# Cache dependencies first
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package \
 && mkdir -p /workspace/app \
 && cp target/*.jar /workspace/app/application.jar

############################################################
# Stage 2 — runtime JRE (non-root, healthcheck-ready)
############################################################
FROM eclipse-temurin:17-jre-alpine AS runtime

LABEL org.opencontainers.image.title="Hospicloud / Shambua Santé API" \
      org.opencontainers.image.description="Backend Spring Boot multi-tenant hospitalier" \
      org.opencontainers.image.vendor="Shambua Santé"

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom" \
    SPRING_PROFILES_ACTIVE=docker \
    SERVER_PORT=8082 \
    APP_ASYNC_STORAGE_DIR=/var/hospicloud/async \
    TZ=UTC

RUN apk add --no-cache curl tzdata \
 && addgroup -S hospicloud \
 && adduser -S -G hospicloud hospicloud \
 && mkdir -p /var/hospicloud/async/reports /var/hospicloud/async/enregistrements /app \
 && chown -R hospicloud:hospicloud /var/hospicloud /app

WORKDIR /app
COPY --from=build --chown=hospicloud:hospicloud /workspace/app/application.jar /app/application.jar

USER hospicloud
EXPOSE 8082

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=5 \
  CMD curl -fsS "http://127.0.0.1:${SERVER_PORT}/actuator/health" || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/application.jar"]
