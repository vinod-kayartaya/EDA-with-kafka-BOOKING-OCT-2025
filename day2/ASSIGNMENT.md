## **Day 2 – Lab challenges (scenario based)**

### **Scenario Theme:**

**FinTech Event Streaming System**

You are building a simplified _Payment Transaction Event System_ for a fintech startup. The company processes thousands of debit and credit transactions per minute.
Your goal is to design producers and consumers using Java and Spring Boot, while enforcing schema validation with Confluent Schema Registry.

### **Lab 1 – Setting up Kafka and Creating Topics**

**Objective:**
Set up Kafka locally (or via Docker) and create topics for financial transactions.

**Scenario:**
You are assigned to prepare a Kafka cluster for the Payments team. The team will use a topic called `TransactionEvents` to publish debit/credit transactions.

**Steps:**

1. Start Zookeeper and Kafka using Docker or CLI.
2. Create a topic:

   ```
   kafka-topics --create --topic TransactionEvents --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
   ```

3. Verify topic creation and describe its configuration.
4. Publish a few test messages using CLI producer and consume them.

**Outcome:**
A working Kafka topic ready to receive and stream transaction events.

### **Lab 2 – Java Kafka Producer and Consumer**

**Objective:**
Create Java-based producer and consumer applications for transaction events.

**Scenario:**
As part of the engineering team, you must develop a producer that sends JSON-formatted transactions and a consumer that processes them for downstream analytics.

**Steps:**

1. Create a Java Maven project and include `kafka-clients` dependency.
2. Implement a `TransactionProducer` class to send messages.
3. Implement a `TransactionConsumer` class to read messages.
4. Demonstrate reading offsets and committing them manually.
5. Print messages with timestamp and partition information.

**Outcome:**
Working Java producer and consumer that exchange messages through Kafka.

### **Lab 3 – Spring Boot Kafka Producer and Consumer**

**Objective:**
Build a Spring Boot microservice that produces and consumes payment transactions.

**Scenario:**
The Payments microservice must publish `PaymentInitiated` events, and the Notification microservice must consume them to trigger alerts.

**Steps:**

1. Create a Spring Boot project with the `spring-kafka` dependency.
2. Configure Kafka properties in `application.yml`.
3. Implement a REST endpoint `/publishPayment` to trigger a producer method using `KafkaTemplate`.
4. Implement a consumer using `@KafkaListener` to receive the events.
5. Verify event flow using application logs.

**Outcome:**
End-to-end event flow between two Spring Boot microservices using Kafka.

### **Lab 4 – Error Handling and Retries**

**Objective:**
Implement error handling, retries, and Dead Letter Topic (DLT) in Spring Boot.

**Scenario:**
Some payment events contain invalid data. You must ensure the consumer retries failed messages and moves persistent failures to a DLT for later inspection.

**Steps:**

1. Configure a DLT topic named `TransactionEvents.DLT`.
2. Add a `DefaultErrorHandler` in Spring configuration with retry logic.
3. Simulate a failure (e.g., missing `transactionId`).
4. Observe automatic retries and redirection to the DLT.

**Outcome:**
Reliable consumer that handles transient and permanent failures gracefully.

### **Lab 5 – Schema Design and Registry Integration**

**Objective:**
Design Avro schemas for events and integrate with Confluent Schema Registry.

**Scenario:**
Your team decides to standardize all payment events using Avro for better validation and compatibility. You must register schemas and use them for message serialization.

**Steps:**

1. Define an Avro schema for `PaymentEvent` with fields like `transactionId`, `amount`, `currency`, and `timestamp`.
2. Register schema in the Schema Registry (`http://localhost:8081`).
3. Update Spring Boot producer and consumer configurations to use Avro serializer/deserializer.
4. Publish and consume Avro events.
5. Try changing the schema to test evolution (e.g., adding a field with a default value).

**Outcome:**
End-to-end Avro-based event flow with schema validation using Schema Registry.
