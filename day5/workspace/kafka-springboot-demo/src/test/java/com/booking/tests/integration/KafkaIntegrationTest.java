package com.booking.tests.integration;

import com.booking.model.avro.BankTransaction;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = "bank-transactions")
class KafkaIntegrationTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    private MockSchemaRegistryClient schemaRegistryClient;
    private KafkaTemplate<String, BankTransaction> kafkaTemplate;

    @BeforeEach
    void setup() {
        schemaRegistryClient = new MockSchemaRegistryClient();

        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafka);
        producerProps.put("key.serializer", StringSerializer.class);
        producerProps.put("value.serializer", KafkaAvroSerializer.class);
        producerProps.put("schema.registry.url", "mock://schema-registry");

        ProducerFactory<String, BankTransaction> producerFactory =
                new DefaultKafkaProducerFactory<>(producerProps);
        kafkaTemplate = new KafkaTemplate<>(producerFactory);
    }

    @Test
    void shouldSendAndReceiveBankTransaction() throws Exception {
        String topic = "bank-transactions";

        BlockingQueue<ConsumerRecord<String, BankTransaction>> records = new LinkedBlockingQueue<>();

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafka);
        consumerProps.put("key.deserializer", StringDeserializer.class);
        consumerProps.put("value.deserializer", KafkaAvroDeserializer.class);
        consumerProps.put("schema.registry.url", "mock://schema-registry");
        consumerProps.put("specific.avro.reader", true);

        ConsumerFactory<String, BankTransaction> consumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProps);

        ContainerProperties containerProps = new ContainerProperties(topic);
        KafkaMessageListenerContainer<String, BankTransaction> container =
                new KafkaMessageListenerContainer<>(consumerFactory, containerProps);
        container.setupMessageListener((MessageListener<String, BankTransaction>) record -> {
            records.add(record);
        });
        container.start();

        // Send message
        BankTransaction txn = BankTransaction.newBuilder()
                .setTransactionId("T3001")
                .setAccountNumber("A900")
                .setAmount(7500.0)
                .setTransactionType("debit")
                .build();

        kafkaTemplate.send(topic, txn.getTransactionId().toString(), txn);

        ConsumerRecord<String, BankTransaction> received = records.poll(10, TimeUnit.SECONDS);
        assertThat(received).isNotNull();
        assertThat(received.value().getTransactionId().toString()).isEqualTo("T3001");
    }
}

