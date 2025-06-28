# resource-server/Dockerfile
FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/resource-server-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]