
FROM openjdk:21-jdk-slim


RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY target/auth-server-0.0.1-SNAPSHOT.jar app.jar
RUN mkdir -p /app/logs /app/data 
ENV SPRING_PROFILES_ACTIVE=docker
ENV DILITHIUM_SECURITY_LEVEL=3
ENV JAVA_OPTS="-Xmx512m -Xms256m -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]