
FROM openjdk:21-jdk-slim

# Instalar dependências para algoritmos pós-quânticos
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY target/auth-server-0.0.1-SNAPSHOT.jar app.jar

# Configurações específicas para Dilithium
ENV SPRING_PROFILES_ACTIVE=default
ENV DILITHIUM_SECURITY_LEVEL=3

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]