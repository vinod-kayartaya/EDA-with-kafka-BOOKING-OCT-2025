# Exception handling in a Kafka consumer

Handling exceptions in a Spring Boot Kafka consumer is important to ensure your application is resilient, does not lose messages, and has a strategy for failure cases. You want a system that does not crash on bad data and provides a clear way to inspect failed messages.

## Key Concepts

1. **Retry**
   When a consumer fails to process a message, Spring Kafka can retry the processing automatically before marking it as failed.

2. **Error Handling**
   You can define error handlers to decide what to do when retries are exhausted.

3. **Dead Letter Topic (DLT)**
   A DLT is a Kafka topic where messages that keep failing beyond retries are redirected for later investigation and manual recovery.

## Options for Exception Handling

Spring Kafka provides different ways to handle consumer failures.

### 1. SeekToCurrentErrorHandler (Legacy)

Earlier versions relied on `SeekToCurrentErrorHandler` to retry by repositioning the consumer offset. This approach is still supported but not recommended for new configurations.

### 2. DefaultErrorHandler (Recommended)

With Spring Kafka 2.8+, `DefaultErrorHandler` handles retries and recovery better. You can configure backoff and DLT automatically.

Example:

```java
@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> kafkaTemplate) {
        // Retry 3 times with 2 seconds interval
        FixedBackOff fixedBackOff = new FixedBackOff(2000L, 3);

        // Create the handler with retry logic
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                new DeadLetterPublishingRecoverer(kafkaTemplate),
                fixedBackOff
        );

        // Ignore specific exceptions if needed
        // errorHandler.addNotRetryableExceptions(IllegalArgumentException.class);

        return errorHandler;
    }
}
```

This configuration does the following:

- Retries message processing 3 times, with an interval of 2 seconds
- If it still fails, publishes the message to a `topic-name-dlt` topic automatically

You only need to make sure a `KafkaTemplate` bean is present.

## DLT (Dead Letter Topic)

A DLT helps you capture problematic messages without blocking the main consumer.

How it works:

- Message fails
- Retried several times
- Still fails
- Automatically sent to `original-topic-name-dlt`

You can create another consumer to read from the DLT:

```java
@KafkaListener(topics = "my-topic-dlt", groupId = "dlt-consumer")
public void consumeDLT(String failedMessage) {
    System.out.println("Message from DLT: " + failedMessage);
    // Investigate and fix
}
```

This keeps your main consumers clean while still providing visibility into issues.

## When Should You Use a DLT

Use DLT when:

- You expect occasional bad data from upstream systems
- You want your consumer to be highly available and not stuck on one poison message
- You need to inspect or reprocess failures later

Avoid DLT when:

- Bad data must block processing to ensure strict correctness
- Message processing must be strongly ordered and not skipped

## Practical Advice

- Always log the failure cause before sending to DLT
- Include metadata like partition, offset, timestamp in DLT messages to simplify analysis
- Monitor DLT volume to catch systemic issues early
- Have a strategy to manually reprocess DLT messages

## Summary

Handling exceptions in Spring Boot Kafka consumers is about resilience:

| Feature       | Purpose                                                   |
| ------------- | --------------------------------------------------------- |
| Retry         | Give the message a few more chances                       |
| Error Handler | Helps decide what to do after retries                     |
| DLT           | Safe place for failed messages so the system doesn’t stop |

Use `DefaultErrorHandler` with `DeadLetterPublishingRecoverer` for modern, reliable exception management.
