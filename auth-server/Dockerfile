# auth-server/Dockerfile
FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/auth-server-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar"