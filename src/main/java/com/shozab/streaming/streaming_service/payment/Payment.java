package com.shozab.streaming.streaming_service.payment;

public class Payment {
    private Long id;
    private Long creditCardNumber;
    private int amount;
    private String payorEmail;

    public Payment(Long id, Long creditCardNumber, int amount, String payorEmail) {
        this.id = id;
        this.creditCardNumber = creditCardNumber;
        this.amount = amount;
        this.payorEmail = payorEmail;
    }
    
    public Long getId() {return id;}
    public Long getCreditCardNumber() {return creditCardNumber;}
    public int getAmount() {return amount;}
    public String getPayorEmail() {return payorEmail;}
}
