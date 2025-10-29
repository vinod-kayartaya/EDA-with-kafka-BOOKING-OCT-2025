package com.booking.bank;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class KafkaTransactionProducer {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        props.put("schema.registry.url", "http://localhost:8081");

        String topic = "transactions";

        KafkaProducer<String, TransactionEvent> producer = new KafkaProducer<>(props);
        TransactionEvent te = TransactionEvent.newBuilder()
                .setAmount(39000)
                .setTxId(333)
                .setTxType("cash")
                .build();

        ProducerRecord<String, TransactionEvent> record =
                new ProducerRecord<>(topic, "tx-"+te.getTxId(), te);
        producer.send(record);
        producer.flush();
        producer.close();
    }
}
