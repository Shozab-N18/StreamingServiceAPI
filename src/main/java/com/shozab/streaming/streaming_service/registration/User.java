package com.shozab.streaming.streaming_service.registration;

import java.time.LocalDate;

public class User {
    private Long id;
    private String username;
    private String password;
    private String email;
    private LocalDate dateOfBirth;
    private Long creditCardNumber;
    
    public User(Long id, String username, String password, String email, LocalDate dateOfBirth, Long creditCardNumber) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.creditCardNumber = creditCardNumber;
    }

    public User(User user) {
        this.id = user.id;
        this.username = user.username;
        this.password = user.password;
        this.email = user.email;
        this.dateOfBirth = user.dateOfBirth;
        this.creditCardNumber = user.creditCardNumber;
    }

    public User() {}

    public Long getId() {return id;}
    public String getUsername() {return username;}
    public String getPassword() {return password;}
    public String getEmail() {return email;}
    public LocalDate getDateOfBirth() {return dateOfBirth;}
    public Long getCreditCardNumber() {return creditCardNumber;}
}
