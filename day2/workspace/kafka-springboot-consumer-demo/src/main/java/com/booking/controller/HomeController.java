package com.booking.controller;

import org.apache.kafka.common.protocol.types.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    public HomeController() {
        System.out.println(">>> HomeController instantiated.");
    }

    @GetMapping("/")
    public String home(){
        return """
                <h1>Welcome to Spring Kafka Consumer Demo app</h1>
                <hr/>
                """;
    }
}
