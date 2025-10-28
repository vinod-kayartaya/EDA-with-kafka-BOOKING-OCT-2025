package com.booking.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerSmsService {

    @KafkaListener(topics = {"cash-deposit", "cheque-deposit"}, groupId = "credit-tx")
    public void sendSmsForDepositEvents(ConsumerRecord<String, String> record){

        System.out.println("================= new kafka message received =================");
        System.out.println("Topic = " + record.topic());
        System.out.println("Partition = " + record.partition());
        System.out.println("Key = " + record.key());
        System.out.println("Value = " + record.value());
        System.out.println("--------------------------------------------------------------");
    }
}
