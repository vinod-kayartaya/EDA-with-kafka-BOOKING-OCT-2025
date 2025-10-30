# Understanding Grouping and Aggregation in Kafka Streams — A Developer’s Deep Dive

When you first start working with **Kafka Streams**, one of the most common sources of confusion is how **grouping** and **aggregation** actually work on an _unbounded stream_ of events.
After all, streams are continuous — so what exactly are we grouping, and when does aggregation “finish”?

In this post, we’ll demystify the concepts of **grouping**, **aggregation**, and **materialization** in Kafka Streams, and then walk through a practical **non-windowed aggregation** example.

## The Nature of Streaming Data

In batch processing systems, you typically deal with _bounded datasets_. You load some data, run a `GROUP BY` operation, and get a result set.

But in **stream processing**, data never stops flowing. Each event arrives one at a time, and your application continuously updates its view of the world as new data comes in.

That’s the mindset shift:

> Kafka Streams doesn’t group over a fixed dataset — it maintains **a continuously updated aggregation**.

## How Grouping Works in Kafka Streams

When you use `groupByKey()` or `groupBy()` in Kafka Streams, the library internally **repartitions** your data so that all records with the same key end up in the same partition.
This ensures that records belonging to the same logical “group” are co-located and can be aggregated together.

For example:

```java
stream.groupByKey()
```

means you’re grouping based on the _existing key_ of each record.
But:

```java
stream.groupBy((key, value) -> extractSomeNewKey(value))
```

means you’re creating a _new key_, so Kafka Streams writes the data to an **internal repartition topic** to realign records by this new grouping key.

Once the grouping is done, each partition can aggregate data for its assigned keys independently.

## Aggregation: Continuous, Not Batch

After grouping, you can perform aggregations such as:

- `count()` — count the number of records per key
- `reduce()` — keep one record per key based on some reduction logic
- `aggregate()` — build any custom aggregate state (sum, average, etc.)

Here’s the key point:

> Aggregation in Kafka Streams is **incremental and stateful**.

Each incoming event updates the **state store** for its key. There’s no concept of “all events are here, now compute.”
Instead, Kafka Streams keeps track of the _latest aggregate per key_, continuously.

## Understanding `Materialized.with(...)`

When you aggregate in Kafka Streams, the system needs to know:

1. How to **serialize and deserialize** the key and value for the state store, and
2. How to **store** the intermediate state.

That’s where `Materialized.with(...)` comes in.

Example:

```java
.groupByKey()
.count(Materialized.with(Serdes.String(), Serdes.Long()));
```

This tells Kafka Streams:

- The key type is `String`
- The value type is `Long`
- Use these SerDes when writing to the local state store (RocksDB) and to the Kafka changelog topic

Without `Materialized.with()`, Kafka Streams will try to infer SerDes automatically, which can fail when types have changed due to prior transformations.

In short:

> `Materialized.with()` ensures your aggregation state is properly typed, stored, and recoverable.

## Example: Non-Windowed Aggregation

Let’s walk through a real example.

### Scenario

You receive transaction messages like:

```
id=123;type=cash;amount=3000
```

You want to:

- Group them by `type` (e.g., `cash`, `card`)
- Continuously sum up the total `amount` for each type
- Produce updated totals to an output topic

### Complete Java Example

```java
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;

import java.util.Properties;

public class TransactionAggregator {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "txn-aggregator-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, String> transactions = builder.stream("transactions");

        KGroupedStream<String, Double> groupedByType = transactions
            .map((key, value) -> {
                String[] parts = value.split(";");
                String type = null;
                double amount = 0.0;
                for (String part : parts) {
                    String[] kv = part.split("=");
                    if (kv.length == 2) {
                        if (kv[0].equals("type")) type = kv[1];
                        else if (kv[0].equals("amount")) amount = Double.parseDouble(kv[1]);
                    }
                }
                return KeyValue.pair(type, amount);
            })
            .groupByKey(Grouped.with(Serdes.String(), Serdes.Double()));

        KTable<String, Double> totalByType = groupedByType.aggregate(
            () -> 0.0,                                // Initializer
            (type, newAmount, agg) -> agg + newAmount, // Aggregator
            Materialized.with(Serdes.String(), Serdes.Double())
        );

        totalByType.toStream().to("aggregated-totals", Produced.with(Serdes.String(), Serdes.Double()));

        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
```

### Sample Input and Output

**Input Topic (`transactions`):**

```
id=101;type=cash;amount=1000
id=102;type=card;amount=500
id=103;type=cash;amount=2000
id=104;type=card;amount=1000
```

**Output Topic (`aggregated-totals`):**

```
(cash, 1000.0)
(card, 500.0)
(cash, 3000.0)
(card, 1500.0)
```

Notice how the totals continuously update as new events arrive.
There’s no time window — the aggregation is **non-windowed** and **unbounded**.

## What Happens Internally

Kafka Streams automatically manages several moving parts behind the scenes:

| Component                 | Purpose                                                       |
| ------------------------- | ------------------------------------------------------------- |
| **Repartition Topic**     | Ensures all records for the same key go to the same partition |
| **State Store (RocksDB)** | Holds the current running total per key                       |
| **Changelog Topic**       | Backs up the state store for fault tolerance                  |
| **KTable**                | Represents the continuously updated view of the aggregation   |

## Non-Windowed vs. Windowed Aggregations

| Type                | Description                           | Example Use Case               |
| ------------------- | ------------------------------------- | ------------------------------ |
| **Non-windowed**    | Aggregates continuously with no reset | Lifetime total per user        |
| **Tumbling window** | Aggregates per fixed time interval    | Sales per minute               |
| **Hopping window**  | Sliding overlapping windows           | Moving average of CPU usage    |
| **Session window**  | Based on activity gaps                | User session duration tracking |

In this example, we’re using the **non-windowed** approach — meaning the state just keeps growing and updating.

## Key Takeaways

- **Grouping** in Kafka Streams ensures all records for a key end up together — sometimes requiring repartitioning.
- **Aggregation** is **continuous and incremental**, not a one-time batch.
- **`Materialized.with()`** defines how your aggregation state is stored and serialized.
- **Non-windowed aggregations** maintain a running total that updates indefinitely.
- Under the hood, Kafka Streams uses **state stores** and **changelog topics** to make all of this fault-tolerant and scalable.

## Final Thoughts

Kafka Streams provides a remarkably elegant abstraction for handling continuous, stateful stream processing.
By understanding how `groupBy`, `aggregate`, and `Materialized` work together, you can build robust, real-time applications that scale naturally as your data flows.

Whether you’re summing transactions, tracking metrics, or maintaining dynamic leaderboards — these are the building blocks that power **real-time analytics pipelines** in Kafka Streams.
