package com.booking.kafka.streams;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;

import java.util.Properties;

public class AlertLargeTransactions {

    public static void main(String[] args) {

        StreamsBuilder builder = new StreamsBuilder();
        String sourceTopic = "transactions";
        String targetTopic = "largeTransactions";

        // represents the source event topic
        KStream<String, String> transactions = builder.stream(sourceTopic);

        // filter transactions with amount >= 50000
        KStream<String, String> largeTransactions =
                transactions.peek((k, v) -> {
                            System.out.printf("transaction: key = %s, value = %s\n", k, v);
                        }).filter((k, v) -> {
                            // value --> id=222;type=cash;amount=34000
                            var data = v.split(";");
                            var amountData = data[2].split("=");
                            var amount = Double.parseDouble(amountData[1]);
                            return amount >= 50000;
                        })
                        .peek((k, v) -> {
                            System.out.printf("large transaction: key = %s, value = %s\n", k, v);
                        });

        largeTransactions.to(targetTopic);

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "bank-transactions");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
