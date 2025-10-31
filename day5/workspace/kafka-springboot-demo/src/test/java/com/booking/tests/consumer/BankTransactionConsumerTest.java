package com.booking.tests.consumer;

import com.booking.consumer.BankTransactionConsumer;
import com.booking.model.avro.BankTransaction;
import org.junit.jupiter.api.Test;

public class BankTransactionConsumerTest {

    @Test
    void shouldConsumeTransaction() {
        BankTransactionConsumer consumer = new BankTransactionConsumer();

        BankTransaction txn = BankTransaction.newBuilder()
                .setTransactionId("T2001")
                .setAccountNumber("A500")
                .setAmount(10000.0)
                .setTransactionType("credit")
                .build();

        consumer.consume(txn);
        // Here we just ensure it doesn’t throw errors. For logic, you’d use assertions.
    }
}

