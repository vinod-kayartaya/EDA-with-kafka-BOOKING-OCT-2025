package com.booking.consumer;


import com.booking.model.avro.BankTransaction;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class BankTransactionConsumer {

    @KafkaListener(topics = "bank-transactions", groupId = "bank-transactions-consumer-1")
    public void consume(BankTransaction transaction) {
        System.out.println("Received transaction: " + transaction);
    }
}

