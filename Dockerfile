FROM eclipse-temurin:21-jre-jammy

RUN apt-get update && \
    apt-get install -y s3270 && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN which s3270

WORKDIR /app

COPY target/tn3270-api-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.net.preferIPv4Stack=true", "-jar", "app.jar"]