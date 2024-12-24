package com.shozab.streaming.streaming_service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.shozab.streaming.streaming_service.payment.Payment;
import com.shozab.streaming.streaming_service.payment.PaymentService;
import com.shozab.streaming.streaming_service.registration.User;
import com.shozab.streaming.streaming_service.registration.UserService;

public class PaymentServiceTest {
    @Mock
    private UserService userService;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test 
    public void testIsCreditCardNumberValid() {
        assertTrue(paymentService.isCreditCardNumberValid(1234567890123456L));
        
        assertThrows(IllegalArgumentException.class, () -> paymentService.isCreditCardNumberValid(123456789012345L));
        assertThrows(IllegalArgumentException.class, () -> paymentService.isCreditCardNumberValid(null));
    }

    @Test
    public void testIsAmountValid() {
        assertTrue(paymentService.isAmountValid(500));
        
        assertThrows(IllegalArgumentException.class, () -> paymentService.isAmountValid(1000));
        assertThrows(IllegalArgumentException.class, () -> paymentService.isAmountValid(0));

        assertThrows(IllegalArgumentException.class, () -> paymentService.isAmountValid(-50));
        assertThrows(IllegalArgumentException.class, () -> paymentService.isAmountValid(1500));
    }

    @Test
    public void testProcessPayment_Success() {
        User user = new User(1L, "johndoe", "Password1", "johndoe@example.com", LocalDate.of(2000, 1, 1), 1234567812345678L);
        Payment payment = new Payment(1L, 1234567812345678L, 500, "johndoe@example.org");
        
        when(userService.findUserByEmail(payment.getPayorEmail())).thenReturn(user);

        paymentService.processPayment(payment);

        assertTrue(paymentService.getPaymentList().containsKey(user.getEmail()));
        assertEquals(payment.getAmount(), paymentService.getPaymentList().get(user.getEmail()).getAmount());
        assertEquals(payment.getCreditCardNumber(), paymentService.getPaymentList().get(user.getEmail()).getCreditCardNumber());
    }

    @Test
    public void testProcessPayment_CreditCardNumberNotFound() {
        Payment payment = new Payment(1L, 1234567812345678L, 123, "johndoe@example.org");
        assertThrows(IllegalStateException.class, () -> paymentService.processPayment(payment), "Payor not found or invalid credit card number");
    }


    @Test
    public void testProcessPayment_InvalidCreditCardNumber() {
        Payment payment1 = new Payment(1L, 12345678L, 123, "johndoe@example.org");
        Payment payment2 = new Payment(1L, null, 123, "johndoe@example.org");
        assertThrows(IllegalArgumentException.class, () -> paymentService.processPayment(payment1), "Invalid credit card number");
        assertThrows(IllegalArgumentException.class, () -> paymentService.processPayment(payment2), "Invalid credit card number");
    }

    @Test
    public void testProcessPayment_InvalidAmount() {
        Payment payment1 = new Payment(1L, 12345678L, 1000, "johndoe@example.org");
        Payment payment2 = new Payment(1L, 12345678L, 0, "johndoe@example.org");

        assertThrows(IllegalArgumentException.class, () -> paymentService.processPayment(payment1), "Invalid payment amount");
        assertThrows(IllegalArgumentException.class, () -> paymentService.processPayment(payment2), "Invalid payment amount");
    }
}