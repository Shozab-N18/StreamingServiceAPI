package com.shozab.streaming.streaming_service.payment;

import org.springframework.stereotype.Service;

import com.shozab.streaming.streaming_service.registration.UserService;
import com.shozab.streaming.streaming_service.registration.User;

import java.util.HashMap;

@Service
public class PaymentService {
    private final UserService userService;
    private final HashMap<String, Payment> paymentList = new HashMap<>();

    public PaymentService(UserService userService) {
        this.userService = userService;
    }
    
    public void processPayment(Payment payment) throws IllegalArgumentException, IllegalStateException {
        try {
            isCreditCardNumberValid(payment.getCreditCardNumber());
            isAmountValid(payment.getAmount());
            
            User user = userService.findUserByEmail(payment.getPayorEmail());
            
            if (user != null && user.getCreditCardNumber().equals(payment.getCreditCardNumber())) {
                paymentList.put(user.getEmail(), payment);
                return;
            }
            
            throw new IllegalStateException("Payor not found or invalid credit card number");
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e;
        }
    }

    public boolean isCreditCardNumberValid(Long creditCardNumber) throws IllegalArgumentException {
        if(creditCardNumber == null || creditCardNumber.toString().length() != 16) {
            throw new IllegalArgumentException("Invalid credit card number");
        } else {
            return true;
        }
    }

    public boolean isAmountValid(int amount) throws IllegalArgumentException {
        if (amount > 0 && amount <= 999) return true;
        else throw new IllegalArgumentException("Invalid payment amount");
    }

    public HashMap<String, Payment> getPaymentList() {
        return paymentList;
    }
}
