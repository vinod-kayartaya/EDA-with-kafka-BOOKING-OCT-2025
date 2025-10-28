# **simple Kafka producer and consumer in Java** using **Maven**,

## **Step 1: Create a Maven Project**

Directory structure:

```
kafka-demo/
 ├─ pom.xml
 └─ src/main/java/com/example/kafka/
       ├─ ProducerExample.java
       └─ ConsumerExample.java
```

## **Step 2: pom.xml**

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example.kafka</groupId>
    <artifactId>kafka-demo</artifactId>
    <version>1.0-SNAPSHOT</version>
    <properties>
        <java.version>17</java.version>
        <kafka.clients.version>3.7.0</kafka.clients.version>
    </properties>
    <dependencies>
        <!-- Kafka client dependency -->
        <dependency>
            <groupId>org.apache.kafka</groupId>
            <artifactId>kafka-clients</artifactId>
            <version>${kafka.clients.version}</version>
        </dependency>

        <!-- Logging -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>2.0.7</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-simple</artifactId>
            <version>2.0.7</version>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <!-- Compiler plugin -->
            <plugin>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>${java.version}</source>
                    <target>${java.version}</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

## **Step 3: Kafka Producer Example**

Create **ProducerExample.java**:

```java
package com.example.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Properties;
import java.util.concurrent.Future;

public class ProducerExample {
    public static void main(String[] args) {
        // Kafka broker address
        String bootstrapServers = "localhost:9092";
        String topic = "test-topic";

        // Producer properties
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            for (int i = 1; i <= 10; i++) {
                String key = "Key-" + i;
                String value = "Message-" + i;

                ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);

                Future<RecordMetadata> future = producer.send(record, (metadata, exception) -> {
                    if (exception != null) {
                        System.out.printf("Error sending message: %s%n", exception.getMessage());
                    } else {
                        System.out.printf("Sent message: key=%s value=%s to partition=%d offset=%d%n",
                                key, value, metadata.partition(), metadata.offset());
                    }
                });

                // Optional: wait for acknowledgment
                future.get();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

✅ **What it does:**

- Connects to Kafka on `localhost:9092`
- Sends 10 messages to `test-topic`
- Prints partition and offset of each sent message

## **Step 4: Kafka Consumer Example**

Create **ConsumerExample.java**:

```java
package com.example.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class ConsumerExample {
    public static void main(String[] args) {
        String bootstrapServers = "localhost:9092";
        String topic = "test-topic";
        String groupId = "test-group";

        // Consumer properties
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("group.id", groupId);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest"); // read from beginning if no offset

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(topic));

            System.out.println("Waiting for messages...");

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) {
                    System.out.printf("Received: key=%s value=%s partition=%d offset=%d%n",
                            record.key(), record.value(), record.partition(), record.offset());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

✅ **What it does:**

- Connects to Kafka on `localhost:9092`
- Joins `test-group` consumer group
- Subscribes to `test-topic`
- Prints any messages received (polling every second)

## **Step 5: Run Your Programs**

1. Start Kafka via Docker Compose (from previous KRaft setup):

```bash
docker compose up -d
```

2. Compile Maven project:

```bash
mvn clean package
```

3. Run Producer:

```bash
mvn exec:java -Dexec.mainClass="com.example.kafka.ProducerExample"
```

4. Run Consumer (in a different terminal):

```bash
mvn exec:java -Dexec.mainClass="com.example.kafka.ConsumerExample"
```

- You should see messages produced by the producer appear in the consumer console.

## ✅ **Optional Improvements**

- Use **async send** and remove `future.get()` for high throughput.
- Add **logging** with SLF4J instead of `System.out`.
- Add **key-based partitioning** logic for ordering.
- In production, configure **security** (SASL, TLS) and handle reconnections.
