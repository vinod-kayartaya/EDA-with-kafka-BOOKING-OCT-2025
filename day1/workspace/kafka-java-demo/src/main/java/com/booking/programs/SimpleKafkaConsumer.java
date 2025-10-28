package com.booking.programs;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

public class SimpleKafkaConsumer {
    private static final String TOPIC = "first-topic";
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String GROUP_ID = "demo-group";

    public static void main(String[] args) {

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(TOPIC));

        System.out.println("Listening to topic: " + TOPIC);

        try {
            while (true) {
                var records = consumer.poll(Duration.ofMillis(5000));
                Set<TopicPartition> tps = consumer.assignment();
                System.out.println("----------------assigned partitions----------------");
                for(TopicPartition tp: tps){
                    System.out.println(tp.topic() + " - " + tp.partition());
                }
                System.out.println("---------------------------------------------------");
                for (var record : records) {
                    System.out.printf("Received => Topic: %s | Partition: %d | Offset: %d | Value: %s%n",
                            record.topic(), record.partition(), record.offset(), record.value());
                }
            }
        } finally {
            consumer.close();
        }
    }
}
