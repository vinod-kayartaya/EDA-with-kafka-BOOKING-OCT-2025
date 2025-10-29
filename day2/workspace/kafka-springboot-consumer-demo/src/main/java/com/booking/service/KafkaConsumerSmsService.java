package com.booking.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerSmsService {

    @KafkaListener(topics = {"cash-deposits", "cheque-deposits"}, groupId = "credit-trx")
    public void sendSmsForDepositEvents(ConsumerRecord<String, String> record){

        System.out.println("================= new kafka message received =================");
        System.out.println("Topic = " + record.topic());
        System.out.println("Partition = " + record.partition());
        System.out.println("Key = " + record.key());
        System.out.println("Value = " + record.value());
        System.out.println("Offset = " + record.offset());
        System.out.println("--------------------------------------------------------------");

        // some business logic that might trigger an exception based
        // on some scenario
        if(record.value().toLowerCase().contains("fail")){
            throw new RuntimeException("Simulation of a consumer failure");
        }
    }
}
