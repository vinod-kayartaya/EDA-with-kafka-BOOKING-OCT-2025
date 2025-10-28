package com.booking.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/deposits")
public class DepositController {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @PostMapping(produces = "application/json")
    public Map<String, Object> handleDeposit(@RequestBody String depositInfo){

        System.out.println("Got this deposit details: " + depositInfo);

        // do some db update logic
        // if successful, send a message to the kafka server

        if(depositInfo.contains("cash-deposit")){
            kafkaTemplate.send("cash-deposits", depositInfo);
        } else if (depositInfo.contains("cheque-deposit")) {
            kafkaTemplate.send("cheque-deposits", depositInfo);
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "success");
        resp.put("timestamp", new Date());
        return resp;
    }
}
