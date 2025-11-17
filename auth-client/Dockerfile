FROM openjdk:21-jdk-slim

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY target/auth-client-0.0.1-SNAPSHOT.jar app.jar

ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Xmx256m -Xms128m"

EXPOSE 8081


HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1


ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]