package com.shozab.streaming.streaming_service.registration;

import java.util.Optional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.validation.ValidationException;

@Service
public class UserService {
    
    public final HashMap<String, User> userList = new HashMap<>();
    
    public UserService() {}
    
    public void registerUser(User user) throws IllegalArgumentException, ValidationException, IllegalStateException {
        try {
            isUsernameValid(user.getUsername());
            isPasswordValid(user.getPassword());
            isEmailValid(user.getEmail());
            isDateOfBirthValid(user.getDateOfBirth());
            isCreditCardNumberValid(user.getCreditCardNumber());
            
            isUserValidAge(user.getDateOfBirth());
            doesUsernameAlreadyExist(user.getUsername());
            doesEmailAlreadyExist(user.getEmail());
            
            // Encode password before saving user to the database
            user = encodePassword(user);
            
            userList.put(user.getEmail(), user);
        } catch (IllegalArgumentException | ValidationException | IllegalStateException e) {
            throw e;
        }
    }
    
	public List<User> getUsers(Optional<Boolean> hasCreditCard) {
        if (!hasCreditCard.isPresent()) return new ArrayList<>(userList.values());
        List<User> filteredUsers = new ArrayList<>();
        
        for (User user : userList.values()) {
            if (hasCreditCard.get() == true && user.getCreditCardNumber() != null) {
                filteredUsers.add(user);
            }
            else if (hasCreditCard.get() == false && user.getCreditCardNumber() == null) {
                filteredUsers.add(user);
            }
        }
        return filteredUsers;
	}

    public User findUserByEmail(String email) {
        if (userList.containsKey(email)) {
            return new User(userList.get(email));
        } else {
            return null;
        }
    }

    public User encodePassword(User user) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode(user.getPassword());
        return new User(
            user.getId(),
            user.getUsername(), 
            encodedPassword, 
            user.getEmail(), 
            user.getDateOfBirth(), 
            user.getCreditCardNumber()
        );
    }

    // Validation methods
    
    /**
     * Username must be at least 5 characters long and contain only letters, digits, underscores and hyphens
     * @return 
     */
    public boolean isUsernameValid(String username) throws IllegalArgumentException {
        if (username != null && username.length() >= 3 && username.matches("^[a-zA-Z0-9_-]*$")) {
            return true;
        } else {
            throw new IllegalArgumentException("Invalid username");
        }
    }

    /**
     * Password must be at least 8 characters long, contain at least one uppercase letter and at least one digit
     * @return
     */
    public boolean isPasswordValid(String password) throws IllegalArgumentException {
        if (password != null && password.length() >= 8 && password.matches(".*[A-Z].*") && password.matches(".*[0-9].*")) {
            return true;
        } else {
            throw new IllegalArgumentException("Invalid password");
        }
    }

    /**
     * Email must start with one or more alphanumeric characters and/or special characters, followed by an @ symbol.
     * After the `@`, it should contain alphanumeric characters and/or dots to represent domains
     * followed by a dot separating the top level domain which should be at least 2 characters long
     * @return
     */
    public boolean isEmailValid(String email) throws IllegalArgumentException {
        if (email != null && email.matches("^[a-zA-Z0-9%+._-]+@[a-zA-Z0-9][a-zA-Z0-9.-]*\\.[a-zA-Z]{2,}$") && !email.contains("..")) {
            return true;
        } else {
            throw new IllegalArgumentException("Invalid email");
        }
    }

    public boolean isDateOfBirthValid(LocalDate dateOfBirth) throws IllegalArgumentException {
        if (dateOfBirth != null && dateOfBirth.isBefore(LocalDate.now())) {
            return true;
        } else {
            throw new IllegalArgumentException("Invalid date of birth");
        }
    } 

    public boolean isUserValidAge(LocalDate dateOfBirth) throws ValidationException {
        if (dateOfBirth != null && dateOfBirth.isBefore(LocalDate.now().minusYears(18))) {
            return true;
        } else {
            throw new ValidationException("User is not old enough");
        }
    }
    
    /**
     * Credit card number can be null or 16 digits long
     * @return
     */
    public boolean isCreditCardNumberValid(Long creditCardNumber) throws IllegalArgumentException {
        if (creditCardNumber == null || creditCardNumber.toString().length() == 16) {
            return true;
        } else {
            throw new IllegalArgumentException("Invalid credit card number");
        }
    }

    public boolean doesUsernameAlreadyExist(String username) throws IllegalStateException {
        for (User user : userList.values()) {
            if (user.getUsername().equals(username)) {
                throw new IllegalStateException("Username already exists");
            }
        }
        return false;
    }

    public boolean doesEmailAlreadyExist(String email) throws IllegalStateException {
        if (userList.containsKey(email)) {
            throw new IllegalStateException("Email already exists");
        } else {
            return false;
        }
    }
}
