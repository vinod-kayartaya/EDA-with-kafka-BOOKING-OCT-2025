# Kafka Streams API

Kafka Streams is a **Java library** used to build **stream processing applications** on top of **Apache Kafka**. It lets you consume data in real time, process or transform it, and produce the results back to Kafka or some external system.

Think of it as a powerful way to **analyze and react to data as it flows**.

## Why does Kafka Streams matter?

Traditional processing often relies on batches. Kafka Streams removes that wait time. It helps you:

• Process data **event by event** in real time
• Build **distributed, scalable** applications
• Keep state within the application using built-in storage
• Avoid needing separate processing clusters (like Spark or Flink)

Your application runs as a normal Java program. Scaling is as simple as running more instances.

## Key Concepts

| Concept          | Meaning                                                                                                   |
| ---------------- | --------------------------------------------------------------------------------------------------------- |
| **Stream**       | A continuous flow of records (like an infinite log).                                                      |
| **Table**        | A changelog representation of data that results in the latest state for each key (like a database table). |
| **KStream**      | API abstraction for streams of events.                                                                    |
| **KTable**       | API abstraction for stateful data representing the latest value for each key.                             |
| **Topology**     | The logical graph of transformations your application performs.                                           |
| **State Stores** | Embedded storage to maintain state for things like windowed counts or aggregations.                       |

## What Can You Do with Streams API?

Kafka Streams supports:

• Filtering records
• Mapping and transforming data
• Aggregations (sum, count, average)
• Joins between streams and/or tables
• Windowing (sliding, tumbling, session windows)
• Stateful processing with fault tolerance

Example scenarios:

• Count the number of orders per minute
• Detect fraud in financial transactions
• Personalize recommendations as events arrive
• Real-time analytics dashboards

## Architecture Advantages

• **Lightweight**: no cluster setup needed.
• **Exactly-once processing** support.
• **Horizontal scaling** based on Kafka partitions.
• **Fully fault tolerant** thanks to Kafka replication and changelogs.

You are not managing servers. Each running instance becomes part of the processing cluster automatically.

## When to use Kafka Streams

Use Kafka Streams when you need:

✓ Real-time event processing
✓ Strong fault tolerance and exactly-once semantics
✓ Easy integration with Kafka
✓ Distributed scale without operational complexity

If your pipeline already centers around Kafka, this is a natural choice.

# Example

Here is a simple and realistic Kafka Streams Java application that does a **real-time word count**. It reads text messages from an input topic, processes them to count the occurrences of each word, and writes the results to an output topic.

## ✅ Scenario

• **Input Topic:** `text-input`
• **Output Topic:** `word-count-output`
• Whenever a line of text arrives on `text-input`, the app updates the running count of each word and publishes the updated counts to `word-count-output`.

## ✅ Maven Dependency (pom.xml)

Make sure to include:

```xml
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-streams</artifactId>
    <version>3.7.0</version>
</dependency>
```

You also need the Kafka Clients if not already included.

## ✅ Java Code Example

```java
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Arrays;
import java.util.Properties;

public class WordCountApp {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordcount-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, String> textLines = builder.stream("text-input");

        textLines
                .flatMapValues(value -> Arrays.asList(value.toLowerCase().split(" ")))
                .groupBy((key, word) -> word)
                .count()
                .toStream()
                .to("word-count-output", Produced.with(Serdes.String(), Serdes.Long()));

        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
```

## ✅ How it works step by step

1. Reads messages from `text-input`
2. Splits lines into individual lowercase words
3. Groups stream by each word
4. Maintains a stateful count
5. Publishes updated counts to `word-count-output`
6. State is automatically stored and recovered if the app restarts

## ✅ Run Order

1. Start Kafka broker (and Zookeeper if your version requires it)
2. Create topics:

```sh
kafka-topics.sh --create --topic text-input --bootstrap-server localhost:9092
kafka-topics.sh --create --topic word-count-output --bootstrap-server localhost:9092
```

3. Run this Streams app
4. Produce some messages into the input topic:

```sh
kafka-console-producer.sh --topic text-input --bootstrap-server localhost:9092
```

Type messages like:

```
hello kafka streams
hello vinod
```

5. Consume results:

```sh
kafka-console-consumer.sh --topic word-count-output --bootstrap-server localhost:9092 --from-beginning --property print.key=true --property print.value=true
```

You will see something like:

```
hello   1
kafka   1
streams 1
hello   2
vinod   1
```

## This example:

• Shows **KStream to KTable** conversion through `.count()`
• Demonstrates **stateful**, fault-tolerant processing
• Shows **event-by-event** updated results

This is the starting point for more real-time analytics and event-driven microservices.

## Java Streams API vs Kafka Streams API

- The Java Stream API (in Java 8) was introduced when Java SE 8 was released in March 2014.
- The Kafka Streams library (as part of Apache Kafka) was first made generally available in version 0.10.0.0 of Kafka, which came out around 2016.

You could technically use the Java Streams API inside a regular Kafka consumer, but it would fall short very quickly. The two “streams” solve very different problems.

### ✅ Java Streams API

• In-memory processing
• Works on finite collections (lists, maps, arrays)
• No built-in fault tolerance
• No state persistence
• Single machine scope
• Not designed for continuous event streams

Java Streams are great for transforming a batch of data that already exists in memory.

### ✅ Kafka Streams API

• Designed for **infinite** streams of real-time data
• Built-in **state stores** for aggregations and joins
• **Fault tolerance** using changelog topics
• **Horizontal scaling** across partitions
• Exactly-once processing
• Handles **rebalancing** automatically
• Time-based windowing, stream-table joins, backpressure handling

Kafka Streams turns your app into a distributed stream processor.

### Where Java Streams fails for Kafka workloads

Imagine you want to maintain a running word count from Kafka.

Java Streams approach:
• Read messages into memory (array or list, wait until a limit)
• Count
• Lose all state when the consumer restarts
• Cannot resume from where it stopped
• Cannot scale beyond one instance safely
• Cannot ensure exactly-once behavior

You would end up writing half of Kafka Streams yourself just to handle fault tolerance and state recovery.

### A clean comparison

| Feature                | Java Streams API       | Kafka Streams API |
| ---------------------- | ---------------------- | ----------------- |
| Handles unbounded data | No                     | Yes               |
| Preserves state        | No                     | Yes               |
| Distributed scaling    | No                     | Yes               |
| Fault tolerance        | No                     | Yes               |
| Exactly-once guarantee | No                     | Yes               |
| Windowing and joins    | Limited to collections | Native, real-time |
| Event time processing  | No                     | Yes               |

## Kafka Streams operations

Here are the most popular and essential Kafka Streams operations you will commonly use in real-time processing.

### 1. **Filtering**

Remove unwanted events.

```java
stream.filter((key, value) -> value.contains("valid"))
```

### 2. **Mapping / Transforming**

Change the key or value.

```java
stream.mapValues(value -> value.toUpperCase())
```

or both key and value:

```java
stream.map((key, value) -> KeyValue.pair(key + "-new", value))
```

### 3. **Flat Mapping**

Expand one event into multiple events.

```java
stream.flatMapValues(value -> Arrays.asList(value.split(" ")))
```

### 4. **Branching**

Split a stream based on conditions.

```java
KStream<String, String>[] branches = stream.branch(
        (k, v) -> v.startsWith("A"),
        (k, v) -> true
);
```

### 5. **Grouping**

Prepare data for aggregations.

```java
KGroupedStream<String, String> grouped = stream.groupBy((key, value) -> value);
```

### 6. **Aggregations**

Running counters or sums.

```java
stream.groupByKey().count();
```

More examples:

```java
.aggregate(() -> 0L, (key, value, agg) -> agg + value)
.reduced(...)
```

### 7. **Windowing**

Aggregation based on time windows.

```java
stream.groupByKey()
     .windowedBy(TimeWindows.ofSizeAndGrace(Duration.ofMinutes(1), Duration.ofSeconds(10)))
     .count();
```

### 8. **Joins**

Enrich streaming data from another stream or table.

Stream–Stream Join:

```java
stream1.join(stream2, (v1, v2) -> v1 + "-" + v2,
              JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofSeconds(30)));
```

Stream–Table Join:

```java
stream.join(table, (value, tableValue) -> value + tableValue)
```

### 9. **Merging**

Combine streams.

```java
stream1.merge(stream2)
```

### 10. **Stateful Processing with Transformers**

Custom logic using state stores.

```java
stream.transform(() -> new MyCustomTransformer(), "state-store-name");
```

### 11. **Materialization**

Persist intermediate results as a KTable/state store.

```java
.groupByKey()
.count(Materialized.as("counts-store"))
```

### 12. **Repartitioning Trigger**

When key must be re-distributed for correct processing.

```java
stream.selectKey((key, value) -> value.getNewKey())
```

#### Simple summary

| Operation Type | Examples                                                      |
| -------------- | ------------------------------------------------------------- |
| Stateless      | filter, map, flatMap, branch, merge                           |
| Stateful       | groupBy, count, reduce, aggregate, join, windowing, transform |
| Conversions    | toTable(), toStream()                                         |
