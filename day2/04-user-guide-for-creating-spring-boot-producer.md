# Build a Spring Boot Kafka Producer from scratch

If you want a minimal, production-ready Kafka producer in Spring Boot, follow these steps. You will create a new project on start.spring.io, wire in the Kafka producer, and expose a REST endpoint that publishes messages to a Kafka topic.

## 1) Generate the project on start.spring.io

1. Go to https://start.spring.io
2. Project: Maven.
3. Language: Java.
4. Spring Boot: 3.5.x.
5. Project Metadata:

   - Group: `com.booking`
   - Artifact: `kafka-springboot-producer-demo`

6. Packaging: Jar
7. Java: 17
8. Dependencies:

   - Spring Web
   - Spring for Apache Kafka

9. Click “Generate” to download the project zip. Extract it and open in your IDE.

The generated POM will look like this after adding the Kafka and Web starters. Your version may differ by a patch number, but these are the important bits: Spring Boot parent, Java 17, `spring-boot-starter-web`, and `spring-kafka`.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>3.5.7</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.booking</groupId>
	<artifactId>kafka-springboot-producer-demo</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>kafka-springboot-producer-demo</name>
	<description>Demo project for consuming kafka messages</description>
	<url/>
	<licenses>
		<license/>
	</licenses>
	<developers>
		<developer/>
	</developers>
	<scm>
		<connection/>
		<developerConnection/>
		<tag/>
		<url/>
	</scm>
	<properties>
		<java.version>17</java.version>
	</properties>
	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.kafka</groupId>
			<artifactId>spring-kafka</artifactId>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.kafka</groupId>
			<artifactId>spring-kafka-test</artifactId>
			<scope>test</scope>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>

</project>
```

## 2) Add application properties

Create or edit `src/main/resources/application.properties` with these entries. This config sets the app name and port, points the producer to your Kafka broker, and uses String serializers for both key and value.

```
spring.application.name=kafka-springboot-producer-demo
server.port=3000

spring.kafka.bootstrap-servers=${KAFKA_BROKER_HOST:localhost}:${KAFKA_BROKER_PORT:9092}
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer
```

Notes:

- `server.port=3000` makes your REST API listen on port 3000.
- `spring.kafka.bootstrap-servers` is set to read `KAFKA_BROKER_HOST` and `KAFKA_BROKER_PORT` from the environment, falling back to `localhost:9092`. This is handy when switching between local, Docker, and server environments without changing code.

## 3) Create a topic name as a property

Add one convenience property (in application.properties file) for your topic. This avoids hard-coding the topic string in multiple places.

```
app.kafka.topic=demo-topic
```

You will use this in the controller.

## 4) Main application class

Your main class is already present from the initializer, typically named something like:

```java
package com.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class KafkaSpringbootProducerDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaSpringbootProducerDemoApplication.class, args);
    }
}
```

No additional config is required here for a basic producer because Spring Boot auto-configures `KafkaTemplate` from the `spring.kafka.*` properties you set.

## 5) Build the REST controller that publishes to Kafka

Create a controller, for example `DepositController`, that accepts an HTTP request and publishes a message to your Kafka topic using `KafkaTemplate<String, String>`.

```java
package com.booking.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deposits")
public class DepositController {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;

    public DepositController(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${app.kafka.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    // Simple JSON payload publisher
    @PostMapping
    public ResponseEntity<String> publish(@RequestBody String payload) {
        kafkaTemplate.send(topic, payload);
        return ResponseEntity.ok("Published to topic '" + topic + "'");
    }

}
```

Why this design works:

- Constructor injection makes the controller testable and clear.
- `KafkaTemplate<String, String>` is auto-configured by Spring Boot because you added `spring-kafka` and set the serializers in `application.properties`.
- The topic is pulled from `app.kafka.topic`.

Adjust partitions and replicas to your environment. If your cluster is a single broker for development, replicas must not exceed 1.

## 6) Run Kafka locally

If you already have a broker at `localhost:9092`, you can skip this. Otherwise, here is a simple Docker Compose you can drop into a `docker-compose.yml` to start Kafka for local development.

```yaml
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    container_name: zookeeper
    restart: always
    networks:
      - kafka-net
    ports:
      - '2181:2181'
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
      ZOOKEEPER_SYNC_LIMIT: 2

  kafka:
    image: confluentinc/cp-kafka:7.4.0
    container_name: kafka
    restart: always
    depends_on:
      - zookeeper
    networks:
      - kafka-net
    ports:
      - '9092:9092'
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: 'zookeeper:2181'
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://10.209.204.132:9092,PLAINTEXT_INTERNAL://kafka:29092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_INTERNAL:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT_INTERNAL
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: 'true'
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1

  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    container_name: kafka-ui
    restart: always
    depends_on:
      - kafka
      - zookeeper
    networks:
      - kafka-net
    ports:
      - '8080:8080'
    environment:
      KAFKA_CLUSTERS_0_NAME: local
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:29092
      KAFKA_CLUSTERS_0_ZOOKEEPER: zookeeper:2181

networks:
  kafka-net:
    driver: bridge
```

Start it with:

```
docker compose up -d
```

## 7) Run the Spring Boot producer

From the project root:

```
./mvnw spring-boot:run
```

You should see the app start on port 3000. If you are not using `localhost:9092`, export the broker variables before running:

```
export KAFKA_BROKER_HOST=your-broker-host
export KAFKA_BROKER_PORT=9092
./mvnw spring-boot:run
```

These align with how `spring.kafka.bootstrap-servers` is defined.

## 8) Test the producer

Create the topic if you did not add the `NewTopic` bean and your cluster does not auto-create topics:

```
docker exec -it kafka bash
kafka-topics --bootstrap-server localhost:9092 --create --topic demo-topic --partitions 3 --replication-factor 1
```

Send a JSON payload:

```
curl -X POST "http://localhost:3000/api/deposits" \
     -H "Content-Type: application/json" \
     -d '{"account":"ACC123","amount":1500,"currency":"INR"}'
```

Or send a quick text:

```
curl -X POST "http://localhost:3000/api/deposits/quick/hello-world"
```

Verify using a console consumer:

```
docker exec -it kafka bash
kafka-console-consumer --bootstrap-server localhost:9092 --topic demo-topic --from-beginning
```

You should see the messages printed in the terminal.
