package com.booking.tests.producer;

import com.booking.model.avro.BankTransaction;
import com.booking.producer.BankTransactionProducer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class BankTransactionProducerTest {

    @Test
    void shouldSendTransactionToKafka() {
        // given
        KafkaTemplate<String, BankTransaction> kafkaTemplate = Mockito.mock(KafkaTemplate.class);
        BankTransactionProducer producer = new BankTransactionProducer(kafkaTemplate);

        BankTransaction txn = BankTransaction.newBuilder()
                .setTransactionId("T1001")
                .setAccountNumber("A200")
                .setAmount(2500.0)
                .setTransactionType("debit")
                .build();

        // when
        producer.sendTransaction(txn);

        // then
        verify(kafkaTemplate, times(1))
                .send(eq("bank-transactions"), eq("T1001"), eq(txn));
    }
}
