package com.booking.bank;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Arrays;
import java.util.Properties;

public class KafkaTransactionConsumer {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "txn-group-1");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        props.put("schema.registry.url", "http://localhost:8081");
        props.put("specific.avro.reader", true);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        String topic = "transactions";
        KafkaConsumer<String, TransactionEvent> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(topic));
        while(true){
            ConsumerRecords<String, TransactionEvent> records = consumer.poll(2000);
            for (ConsumerRecord<String, TransactionEvent> record : records) {
                TransactionEvent txn = record.value();
                System.out.println("--------------------------------");
                System.out.printf("ID       = %d\n", txn.getTxId());
                System.out.printf("Type     = %s\n", txn.getTxType());
                System.out.printf("Amount   = %.2f\n", txn.getAmount());
                System.out.println("--------------------------------");
            }
        }
    }
}
