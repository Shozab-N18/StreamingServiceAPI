package com.shozab.streaming.streaming_service;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;

import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shozab.streaming.streaming_service.registration.*;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.mockito.Mockito.*; 

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shozab.streaming.streaming_service.payment.Payment;
import com.shozab.streaming.streaming_service.payment.PaymentController;
import com.shozab.streaming.streaming_service.payment.PaymentService;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest
@AutoConfigureMockMvc
public class PaymentControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Mock
    private PaymentService paymentService;
    
    @Autowired
    private ObjectMapper oMapper;

    @Mock
    private PaymentController paymentController;

    @Mock
    private UserService userService;
    
    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        ObjectMapper oMapper = new ObjectMapper();
        oMapper.registerModule(new JavaTimeModule());

        User validUser = new User(1L, "johndoe", "Password1", "johndoe@example.org", LocalDate.of(2003, 1, 1), 1234567812345678L);
        
        doNothing().when(userService).registerUser(validUser);

        mockMvc.perform(post("/users/register")
            .contentType("application/json")
            .content(oMapper.writeValueAsString(validUser)))
            .andExpect(status().is(201))
            .andExpect(content().string("User registered successfully"));
    }
    
    @Test
    public void testProcessPayment_Success() throws Exception {
        Payment payment = new Payment(1L, 1234567812345678L, 100, "johndoe@example.org");
        
        doNothing().when(paymentService).processPayment(payment);

        mockMvc.perform(post("/payments")
                .contentType("application/json")
                .content(oMapper.writeValueAsString(payment)))
                .andExpect(status().is(201))
                .andExpect(content().string("Payment processed successfully"));
    }

    @ParameterizedTest
    @CsvSource({
        "1, 12345678, 100",
        "2, 123456781234567812, 999",
    })
    public void testProcessPayment_shortCreditCardNumber(String id, String creditCardNumber, int amount) throws Exception {
        Payment payment = new Payment(Long.parseLong(id), Long.parseLong(creditCardNumber), amount, "johndoe@example.org");
        
        doThrow(new IllegalArgumentException("Invalid credit card number")).when(paymentService).processPayment(payment);

        mockMvc.perform(post("/payments")
                .contentType("application/json")
                .content(oMapper.writeValueAsString(payment)))
                .andExpect(status().is(400))
                .andExpect(content().string("Invalid credit card number"));
    }

    @Test
    public void testProcessPayment_nullCreditCardNumber() throws Exception {
        Payment payment = new Payment(1L, null, 100, "johndoe@example.org");
        
        doThrow(new IllegalArgumentException("Invalid credit card number")).when(paymentService).processPayment(payment);

        mockMvc.perform(post("/payments")
                .contentType("application/json")
                .content(oMapper.writeValueAsString(payment)))
                .andExpect(status().is(400))
                .andExpect(content().string("Invalid credit card number"));
    }

    @Test
    public void testProcessPayment_creditCardNumberNotFound() throws Exception {
        Payment payment = new Payment(1L, 1234567812345671L, 100, "johndoe@example.org");
        
        doThrow(new IllegalStateException("Payor not found or invalid credit card number")).when(paymentService).processPayment(payment);

        mockMvc.perform(post("/payments")
                .contentType("application/json")
                .content(oMapper.writeValueAsString(payment)))
                .andExpect(status().is(404))
                .andExpect(content().string("Payor not found or invalid credit card number"));
    }

    @Test
    public void testProcessPayment_InvalidAmount() throws Exception {
        Payment payment = new Payment(1L, 1234567812345678L, 1000, "johndoe@example.org");
        
        doThrow(new IllegalArgumentException("Invalid payment amount")).when(paymentService).processPayment(payment);

        mockMvc.perform(post("/payments")
                .contentType("application/json")
                .content(oMapper.writeValueAsString(payment)))
                .andExpect(status().is(400))
                .andExpect(content().string("Invalid payment amount"));
    }
}
