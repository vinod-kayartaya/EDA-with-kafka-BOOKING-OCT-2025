package com.booking.controller;



import com.booking.model.avro.BankTransaction;
import com.booking.producer.BankTransactionProducer;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class BankTransactionController {

    private final BankTransactionProducer producer;

    public BankTransactionController(BankTransactionProducer producer) {
        this.producer = producer;
    }

    @PostMapping
    public String createTransaction(@RequestBody BankTransaction transaction) {
        producer.sendTransaction(transaction);
        return "Transaction sent successfully";
    }
}
