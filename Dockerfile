FROM --platform=linux/amd64 openjdk:18

EXPOSE 8080

WORKDIR /app

COPY ./target/PizzaDronz-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]