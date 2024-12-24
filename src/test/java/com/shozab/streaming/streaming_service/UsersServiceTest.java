package com.shozab.streaming.streaming_service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.shozab.streaming.streaming_service.registration.User;
import com.shozab.streaming.streaming_service.registration.UserService;

import jakarta.validation.ValidationException;

@SpringBootTest
public class UsersServiceTest {

    @Mock
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService();
    }

    @Test
    public void testGetUsers_NoCreditCardFilter() {
        userService.registerUser(new User(1L, "johndoe", "Password1", "johndoe@example.com", LocalDate.of(2000, 1, 1), 1234567812345678L)); // has credit card
        userService.registerUser(new User(2L, "jane_doe", "Password2", "janedoe@example.com", LocalDate.of(1995, 5, 15), null)); // no credit card
    
        assertEquals(2, userService.getUsers(Optional.empty()).size());
    }

    @Test
    public void testGetUsers_WithCreditCardFilterTrue() {
        userService.registerUser(new User(1L, "johndoe", "Password1", "johndoe@example.com", LocalDate.of(2000, 1, 1), 1234567812345678L)); // has credit card
        userService.registerUser(new User(2L, "jane_doe", "Password2", "janedoe@example.com", LocalDate.of(1995, 5, 15), null)); // no credit card
    
        List<User> usersWithCreditCard = userService.getUsers(Optional.of(true));

        assertEquals(1, usersWithCreditCard.size());
        assertEquals("johndoe", usersWithCreditCard.get(0).getUsername());
    }
    
    @Test
    public void testGetUsers_WithCreditCardFilterFalse() {
        userService.registerUser(new User(1L, "johndoe", "Password1", "johndoe@example.com", LocalDate.of(2000, 1, 1), 1234567812345678L)); // has credit card
        userService.registerUser(new User(2L, "jane_doe", "Password2", "janedoe@example.com", LocalDate.of(1995, 5, 15), null)); // no credit card
    
        List<User> usersWithoutCreditCard = userService.getUsers(Optional.of(false));

        assertEquals(1, usersWithoutCreditCard.size());
        assertEquals("jane_doe", usersWithoutCreditCard.get(0).getUsername());
    }

    @Test
    public void testRegisterUser_Success() {
        User validUser = new User(1L, "johndoe", "Password1", "john-doe.berk@example.co.uk", LocalDate.of(2000, 1, 1), null);
        
        userService.registerUser(validUser);
        
        User savedUser = userService.findUserByEmail(validUser.getEmail());
        assertNotNull(savedUser);
        assertEquals(savedUser.getId(), validUser.getId());
        assertEquals(savedUser.getUsername(), validUser.getUsername());
        
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        assertTrue(encoder.matches(validUser.getPassword(), savedUser.getPassword()));
        
        assertEquals(savedUser.getEmail(), validUser.getEmail());
        assertEquals(savedUser.getDateOfBirth(), validUser.getDateOfBirth());
        assertEquals(savedUser.getCreditCardNumber(), validUser.getCreditCardNumber());
    }

    @Test
    public void testRegisterUser_InvalidUsername() {
        User invalidUserUsername1 = new User(1L, "", "Password1", "johndoe@example.org", LocalDate.of(2000, 1, 1), null);
        User invalidUserUsername2 = new User(1L, null, "Password1", "johndoe@example.org", LocalDate.of(2000, 1, 1), null);
        User invalidUserUsername3 = new User(1L, "john doe", "Password1", "johndoe@example.org", LocalDate.of(2000, 1, 1), null);
        User invalidUserUsername4 = new User(1L, "john%.doe$", "Password1", "johndoe@example.org", LocalDate.of(2000, 1, 1), null);
        
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(invalidUserUsername1), "Invalid username");
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(invalidUserUsername2), "Invalid username");
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(invalidUserUsername3), "Invalid username");
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(invalidUserUsername4), "Invalid username");
    }   

    @Test
    public void testRegisterUser_InvalidPassword() {
        User invalidUserPassword1 = new User(1L, "johndoe", "", "johndoe@example.org", LocalDate.of(2000, 1, 1), null);
        User invalidUserPassword2 = new User(1L, "johndoe", null, "johndoe@example.org", LocalDate.of(2000, 1, 1), null);
        User invalidUserPassword3 = new User(1L, "johndoe", "password", "johndoe@example.org", LocalDate.of(2000, 1, 1), null);
        User invalidUserPassword4 = new User(1L, "johndoe", "password1", "johndoe@example.org", LocalDate.of(2000, 1, 1), null);
        
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(invalidUserPassword1), "Invalid password");
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(invalidUserPassword2), "Invalid password");
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(invalidUserPassword3), "Invalid password");
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(invalidUserPassword4), "Invalid password");
    }

    @Test
    public void testRegisterUser_InvalidEmail() {
        User invalidUserEmail1 = new User(1L, "johndoe", "Password1", "", LocalDate.of(2000, 1, 1), null);
        User invalidUserEmail2 = new User(1L, "johndoe", "Password1", null, LocalDate.of(2000, 1, 1), null);
        User invalidUserEmail3 = new User(1L, "johndoe", "Password1", "johndoe@example", LocalDate.of(2000, 1, 1), null);
        User invalidUserEmail4 = new User(1L, "johndoe", "Password1", "johndoe.example.org", LocalDate.of(2000, 1, 1), null);
        User invalidUserEmail5 = new User(1L, "johndoe", "Password1", "johndoe@example", LocalDate.of(2000, 1, 1), null);

        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(invalidUserEmail1), "Invalid email");
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(invalidUserEmail2), "Invalid email");
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(invalidUserEmail3), "Invalid email");
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(invalidUserEmail4), "Invalid email");
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(invalidUserEmail5), "Invalid email");
    }

    @Test
    public void testRegisterUser_InvalidDateOfBirth() {
        User invalidUserDateOfBirth1 = new User(1L, "johndoe", "Password1", "johndoe@example.org", null, null);
        User invalidUserDateOfBirth2 = new User(1L, "johndoe", "Password1", "johndoe@example.org", LocalDate.of(2221, 1, 1), null);

        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(invalidUserDateOfBirth1), "Invalid date of birth");
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(invalidUserDateOfBirth2), "Invalid date of birth");
    }

    @Test
    public void testRegisterUser_InvalidCreditCardNumber() {
        User invalidUserCreditCardNumber1 = new User(1L, "johndoe", "Password1", "johndoe@example.org", LocalDate.of(2000, 1, 1), 1234567812345678123L);
        User invalidUserCreditCardNumber2 = new User(1L, "johndoe", "Password1", "johndoe@example.org", LocalDate.of(2000, 1, 1), 12345678L);

        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(invalidUserCreditCardNumber1), "Invalid credit card number");
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(invalidUserCreditCardNumber2), "Invalid credit card number");
    }

    @Test
    public void testRegisterUser_UserNotOldEnough() {
        User invalidUserAge1 = new User(1L, "johndoe", "Password1", "johndoe@example.org", LocalDate.of(2017, 1, 1), null);
        User invalidUserAge2 = new User(1L, "johndoe", "Password1", "johndoe@example.org", LocalDate.of(2007, 1, 1), null);

        assertThrows(ValidationException.class, () -> userService.registerUser(invalidUserAge1), "User is not old enough");
        assertThrows(ValidationException.class, () -> userService.registerUser(invalidUserAge2), "User is not old enough");
    }

    @Test
    public void testRegisterUser_UsernameAlreadyExists() {
        User validUser = new User(1L, "johndoe", "Password1", "johndoe@example.org", LocalDate.of(2000, 1, 1), null);
        userService.registerUser(validUser);

        User duplicateUserUsername = new User(2L, "johndoe", "Password1", "john@example.org", LocalDate.of(2000, 1, 1), null);
        assertThrows(IllegalStateException.class, () -> userService.registerUser(duplicateUserUsername), "Username already exists");
    }

    @Test
    public void testRegisterUser_EmailAlreadyExists() {
        User validUser = new User(1L, "johndoe", "Password1", "johndoe@example.org", LocalDate.of(2000, 1, 1), null);
        userService.registerUser(validUser);

        User duplicateUserEmail = new User(2L, "johndoe", "Password1", "johndoe@example.org", LocalDate.of(2000, 1, 1), null);

        assertThrows(IllegalStateException.class, () -> userService.registerUser(duplicateUserEmail), "Email already exists");
    }

    @Test
    public void testEmailValidation() {
        // Valid email test cases
        assertTrue(userService.isEmailValid("john.doe@example.com")); 
        assertTrue(userService.isEmailValid("jane_doe123@example.co.uk"));
        assertTrue(userService.isEmailValid("a+b%@example.com"));
        assertTrue(userService.isEmailValid("email@subdomain.example.com"));
        assertTrue(userService.isEmailValid("valid-email@domain123.org"));

        // Invalid email test cases
        assertThrows(IllegalArgumentException.class, () -> userService.isEmailValid("")); 
        assertThrows(IllegalArgumentException.class, () -> userService.isEmailValid(null)); 
        assertThrows(IllegalArgumentException.class, () -> userService.isEmailValid("user.com")); // Test no @
        assertThrows(IllegalArgumentException.class, () -> userService.isEmailValid("user@")); // Test no domain
        assertThrows(IllegalArgumentException.class, () -> userService.isEmailValid("@domain.com")); // Test no address
        assertThrows(IllegalArgumentException.class, () -> userService.isEmailValid("user@domain@domain.com")); 
        assertThrows(IllegalArgumentException.class, () -> userService.isEmailValid("user@domain..com"));
        assertThrows(IllegalArgumentException.class, () -> userService.isEmailValid("user@doma,in.com"));
        assertThrows(IllegalArgumentException.class, () -> userService.isEmailValid("user@domain_com.com"));
        assertThrows(IllegalArgumentException.class, () -> userService.isEmailValid("user@doma#in.com")); 
        assertThrows(IllegalArgumentException.class, () -> userService.isEmailValid("user@.example.com")); // Test dot at the beginning of domain
        assertThrows(IllegalArgumentException.class, () -> userService.isEmailValid("user@ex..ample.com"));
        assertThrows(IllegalArgumentException.class, () -> userService.isEmailValid("user@com")); // Test no domain
        assertThrows(IllegalArgumentException.class, () -> userService.isEmailValid("user@domain.c")); // Test short domain
        assertThrows(IllegalArgumentException.class, () -> userService.isEmailValid("user@domain.c$om")); 
        assertThrows(IllegalArgumentException.class, () -> userService.isEmailValid("user@domain$com"));
        assertThrows(IllegalArgumentException.class, () -> userService.isEmailValid("user@domain....com"));
    }

    @Test
    public void testCreditCardValidation() {
        assertTrue(userService.isCreditCardNumberValid(1234567812345678L));
        assertTrue(userService.isCreditCardNumberValid(null)); 

        // Invalid test cases
        assertThrows(IllegalArgumentException.class,  () -> userService.isCreditCardNumberValid(12345678123456789L));
        assertThrows(IllegalArgumentException.class,  () -> userService.isCreditCardNumberValid(1234567812345L));
    }

    @Test
    public void testPasswordValidation() {
        assertTrue(userService.isPasswordValid("Password123")); 
        assertTrue(userService.isPasswordValid("Password@123!")); 
        
        // Invalid test cases
        assertThrows(IllegalArgumentException.class, () -> userService.isPasswordValid("password")); 
        assertThrows(IllegalArgumentException.class, () -> userService.isPasswordValid("PASSWORD")); 
        assertThrows(IllegalArgumentException.class, () -> userService.isPasswordValid("123456789")); 
        assertThrows(IllegalArgumentException.class, () -> userService.isPasswordValid(null)); 
        assertThrows(IllegalArgumentException.class, () -> userService.isPasswordValid("")); 
    }

    @Test
    public void testDateOfBirthValidation() {
        assertTrue(userService.isDateOfBirthValid(LocalDate.of(2000, 1, 1)));
        
        // Invalid date of birth test cases
        assertThrows(IllegalArgumentException.class, () -> userService.isDateOfBirthValid(null)); 
        assertThrows(IllegalArgumentException.class, () -> userService.isDateOfBirthValid(LocalDate.of(2221, 1, 1))); 
        assertThrows(DateTimeException.class, () -> userService.isDateOfBirthValid(LocalDate.of(2000, 1, 35))); 
        assertThrows(DateTimeException.class, () -> userService.isDateOfBirthValid(LocalDate.of(2000, 15, 1))); 
    }

    @Test
    public void testIsUserValidAge() {
        assertTrue(userService.isUserValidAge(LocalDate.of(2000, 1, 1))); 
        assertThrows(ValidationException.class, () -> userService.isUserValidAge(LocalDate.of(2020, 1, 1))); 
        assertThrows(ValidationException.class, () -> userService.isUserValidAge(null)); 
    }

    @Test
    public void testEmailAlreadyExists() {
        User validUser = new User(1L, "johndoe", "Password1", "johndoe@example.org", LocalDate.of(2000, 1, 1), null);
        userService.registerUser(validUser);

        assertThrows(IllegalStateException.class, () -> userService.doesEmailAlreadyExist("johndoe@example.org"), "Email already exists");
    }

    @Test
    public void testFindUserByEmail() {
        User validUser = new User(1L, "johndoe", "Password1", "johndoe@example.org", LocalDate.of(2000, 1, 1), null);
        userService.registerUser(validUser);

        User foundUser = userService.findUserByEmail("johndoe@example.org");
        assertNotNull(foundUser);
        assertEquals(foundUser.getId(), validUser.getId());
        assertEquals(foundUser.getUsername(), validUser.getUsername());
        assertEquals(foundUser.getEmail(), validUser.getEmail());
        assertEquals(foundUser.getDateOfBirth(), validUser.getDateOfBirth());
        assertEquals(foundUser.getCreditCardNumber(), validUser.getCreditCardNumber());

        User foundUserNull = userService.findUserByEmail("janedoe@example.org");
        assertEquals(null, foundUserNull);
    }
}
