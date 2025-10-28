package com.booking.programs;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.Scanner;

public class SimpleKafkaProducer {

    private static final String TOPIC = "first-topic";
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    public static void main(String[] args) {

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter messages to send to Kafka (type 'exit' to quit):");

        while (true) {
            String message = scanner.nextLine();
            if (message.equalsIgnoreCase("exit")) break;

            ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, message);
            producer.send(record, (metadata, exception) -> {
                if (exception != null)
                    System.err.println("Error sending message: " + exception.getMessage());
                else
                    System.out.println("Message sent to: "
                            + metadata.topic() + " | Partition: " + metadata.partition()
                            + " | Offset: " + metadata.offset());
            });
        }

        producer.close();
        scanner.close();
        System.out.println("Producer closed");
    }
}
