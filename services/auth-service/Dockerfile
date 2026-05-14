# ─── Build Stage ─────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apk add --no-cache maven && \
    mvn clean package -DskipTests

# ─── Runtime Stage ───────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

# Usuário não-root por segurança
RUN addgroup -S vaultbank && adduser -S vaultbank -G vaultbank
USER vaultbank

WORKDIR /app

COPY --from=builder /app/target/auth-service-*.jar app.jar

# JVM tuning para containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD wget -qO- http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
