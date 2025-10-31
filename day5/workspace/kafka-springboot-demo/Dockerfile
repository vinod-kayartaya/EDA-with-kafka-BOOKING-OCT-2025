FROM openjdk:17-alpine
WORKDIR /mywordir
COPY ./target/kafka-avro-springboot-0.0.1-SNAPSHOT.jar ./app.jar
EXPOSE 1234
CMD ["java", "-jar", "app.jar"]
