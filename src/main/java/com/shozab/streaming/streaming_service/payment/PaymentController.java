package com.shozab.streaming.streaming_service.payment;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/payments")
public class PaymentController {
    private PaymentService paymentService;
    
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    
    @PostMapping
    public ResponseEntity<String> processPayment(@RequestBody Payment payment) {
        try {
            paymentService.processPayment(payment);
            return ResponseEntity.status(201).body("Payment processed successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage()); // Bad Request
        } catch (IllegalStateException e) {
            return ResponseEntity.status(404).body(e.getMessage()); // Not Found
        }
    }
}
