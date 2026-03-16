FROM eclipse-temurin:21-jre-jammy

RUN apt-get update && \
    apt-get install -y s3270 locales fonts-ipafont-gothic && \
    locale-gen ja_JP.UTF-8 && \
    update-locale LANG=ja_JP.UTF-8 && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

ENV LANG ja_JP.UTF-8
ENV LC_ALL ja_JP.UTF-8

RUN which s3270 && ln -sf /usr/bin/s3270 /usr/bin/ws3270

WORKDIR /app

COPY target/tn3270-api-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 80

ENTRYPOINT ["java", "-Djava.net.preferIPv4Stack=true", "-jar", "app.jar"]