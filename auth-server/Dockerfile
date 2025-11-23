FROM eclipse-temurin:21-jdk-alpine
RUN apk add --no-cache curl
WORKDIR /app
COPY target/auth-server-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080 9080
ENTRYPOINT ["java", "-jar", "app.jar"]
