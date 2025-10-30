package com.booking.streams;

import com.booking.model.Transaction;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

@Configuration
public class IdentifyLargeTransactions {

    @Bean
    public KStream<String, Transaction> kStream(StreamsBuilder builder) {

        JsonSerde<Transaction> txnSerde = new JsonSerde<>(Transaction.class);

        // represents the source topic
        KStream<String, Transaction> stream =
                builder.stream("bank-raw-txns", Consumed.with(Serdes.String(), txnSerde));

        // represents the target topic
        KStream<String, Transaction> largeTxns =
                stream.filter((k, t) -> t.getAmount() >= 50000);

        // TODO: another topic to get the running total of all transactions
        KTable<String, Double> runningTotal =
                stream.groupByKey()
                        .aggregate(() -> 0.0,
                                (k, t, total) -> total + t.getAmount(),
                                Materialized.with(Serdes.String(), Serdes.Double()));

        runningTotal.toStream()
                .peek((k, v) -> System.out.printf("running total = %.2f\n", v))
                .to("bank-running-total");

        largeTxns.to("bank-lg-txns");

        return stream;
    }
}
