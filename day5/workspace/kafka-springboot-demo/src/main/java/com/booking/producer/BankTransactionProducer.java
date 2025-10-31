package com.booking.producer;

import com.booking.model.avro.BankTransaction;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class BankTransactionProducer {

    private final KafkaTemplate<String, BankTransaction> kafkaTemplate;

    public BankTransactionProducer(KafkaTemplate<String, BankTransaction> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendTransaction(BankTransaction transaction) {
        kafkaTemplate.send("bank-transactions", transaction.getTransactionId().toString(), transaction);
        System.out.println("Sent transaction: " + transaction);
    }
}

