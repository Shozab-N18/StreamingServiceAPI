package com.shozab.streaming.streaming_service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shozab.streaming.streaming_service.registration.*;

import jakarta.validation.ValidationException;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.mockito.Mockito.*; 

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Autowired
    private ObjectMapper oMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void tearDown() {
        Mockito.reset(userService);
    }

    @Test
    public void testRegisterUser_Success() throws Exception {
        User validUser = new User(1L, "johndoe", "Password1", "johndoe@example.org", LocalDate.of(2003, 1, 1), 1234567812345678L);
        
        doNothing().when(userService).registerUser(validUser);

        mockMvc.perform(post("/users/register")
            .contentType("application/json")
            .content(oMapper.writeValueAsString(validUser)))
            .andExpect(status().is(201))
            .andExpect(content().string("User registered successfully"));
    }
    
    @ParameterizedTest
    @CsvSource({
        "john doe, Password1,  johndoe@example.org,  2003-01-01, 1234567812345678, Invalid username",
        "johndoe,  password,   johndoe@example.org,  2003-01-01, 1234567812345678, Invalid password",
        "johndoe,  Password1,  johndoe@example,      2003-01-01, 1234567812345678, Invalid email",
        "johndoe,  Password1,  johndoe@example.org,  2026-01-01, 1234567812345678, Invalid date of birth",
        "johndoe,  Password1,  johndoe@example.org,  2003-01-01, 12345678123456,   Invalid credit card number"
    })
    public void testRegisterUser_InvalidArguments(String username, String password, String email, LocalDate dateOfBirth, Long creditCardNumber, String expectedErrorMessage) throws Exception {
        User invalidUser = new User(1L, username, password, email, dateOfBirth, creditCardNumber);
        
        doThrow(new IllegalArgumentException(expectedErrorMessage)).when(userService).registerUser(invalidUser);
        
        mockMvc.perform(post("/users/register")
            .contentType("application/json")
            .content(oMapper.writeValueAsString(invalidUser)))
            .andExpect(status().is(400))
            .andExpect(content().string(expectedErrorMessage));
    }
    
    @Test
    public void testRegisterUser_ValidationException() throws Exception {
        User underagedUser = new User(1L, "johndoe", "Password1", "johndoe@example.org", LocalDate.of(2017, 1, 1), 1234567812345678L);
        
        doThrow(new ValidationException("User is not old enough")).when(userService).registerUser(underagedUser);

        mockMvc.perform(post("/users/register")
            .contentType("application/json")
            .content(oMapper.writeValueAsString(underagedUser)))
            .andExpect(status().is(403))
            .andExpect(content().string("User is not old enough"));
    }

    @Test
    public void testRegisterUser_IllegalStateException() throws Exception {
        User validUser = new User(1L, "johndoe", "Password1", "johndoe@example.org", LocalDate.of(2003, 1, 1), 1234567812345678L);
        User duplicateUserUsername = new User(2L, "johndoe", "Password1", "johndoe@example.org", LocalDate.of(2003, 1, 1), 1234567812345678L);
        
        doNothing().when(userService).registerUser(validUser);

        mockMvc.perform(post("/users/register")
            .contentType("application/json")
            .content(oMapper.writeValueAsString(validUser)))
            .andExpect(status().is(201))
            .andExpect(content().string("User registered successfully"));

        doThrow(new IllegalStateException("Username already exists")).when(userService).registerUser(duplicateUserUsername);

        mockMvc.perform(post("/users/register")
            .contentType("application/json")
            .content(oMapper.writeValueAsString(duplicateUserUsername)))
            .andExpect(status().is(409))
            .andExpect(content().string("Username already exists"));
    }
    
    @Test
    public void testGetUsers_NoCreditCardFilter() throws Exception {
        User user1 = new User(1L, "johndoe", "Password1", "johndoe@example.org", LocalDate.of(2003, 1, 1), 1234567812345678L);
        User user2 = new User(2L, "janedoe", "Password1", "janedoe@example.org", LocalDate.of(2003, 1, 1), null);
        
        doNothing().when(userService).registerUser(user1);

        mockMvc.perform(post("/users/register")
            .contentType("application/json")
            .content(oMapper.writeValueAsString(user1)))
            .andExpect(status().isCreated())
            .andExpect(content().string("User registered successfully"));

        doNothing().when(userService).registerUser(user2);

        mockMvc.perform(post("/users/register")
            .contentType("application/json")
            .content(oMapper.writeValueAsString(user2)))
            .andExpect(status().isCreated())
            .andExpect(content().string("User registered successfully"));

        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);

        when(userService.getUsers(Optional.empty())).thenReturn(users);

        mockMvc.perform(get("/users"))
            .andExpect(status().is(200))
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.size()").value(2))
            .andExpect(jsonPath("$[?(@.id == 1)]").exists())
            .andExpect(jsonPath("$[?(@.username == 'johndoe')]").exists())
            .andExpect(jsonPath("$[?(@.email == 'johndoe@example.org')]").exists())
            .andExpect(jsonPath("$[?(@.dateOfBirth == '2003-01-01')]").exists())
            .andExpect(jsonPath("$[?(@.creditCardNumber == 1234567812345678)]").exists())

            .andExpect(jsonPath("$[?(@.id == 2)]").exists())
            .andExpect(jsonPath("$[?(@.username == 'janedoe')]").exists())
            .andExpect(jsonPath("$[?(@.email == 'janedoe@example.org')]").exists())
            .andExpect(jsonPath("$[?(@.dateOfBirth == '2003-01-01')]").exists())
            .andExpect(jsonPath("$[?(@.creditCardNumber == null)]").exists());
    }

    @Test
    public void testGetUsers_WithCreditCardTrue() throws Exception {
        User user1 = new User(1L, "johndoe", "Password1", "johndoe@example.org", LocalDate.of(2003, 1, 1), 1234567812345678L);
        User user2 = new User(2L, "janedoe", "Password1", "janedoe@example.org", LocalDate.of(2003, 1, 1), null);
        
        doNothing().when(userService).registerUser(user1);

        mockMvc.perform(post("/users/register")
            .contentType("application/json")
            .content(oMapper.writeValueAsString(user1)))
            .andExpect(status().isCreated())
            .andExpect(content().string("User registered successfully"));

        doNothing().when(userService).registerUser(user2);

        mockMvc.perform(post("/users/register")
            .contentType("application/json")
            .content(oMapper.writeValueAsString(user2)))
            .andExpect(status().isCreated())
            .andExpect(content().string("User registered successfully"));

        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);

        when(userService.getUsers(Optional.of(true))).thenReturn(users);

        mockMvc.perform(get("/users?hasCreditCard=yes"))
            .andExpect(status().is(200))
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.size()").value(1))
            .andExpect(jsonPath("$[?(@.id == 1)]").exists())
            .andExpect(jsonPath("$[?(@.username == 'johndoe')]").exists())
            .andExpect(jsonPath("$[?(@.email == 'johndoe@example.org')]").exists())
            .andExpect(jsonPath("$[?(@.dateOfBirth == '2003-01-01')]").exists())
            .andExpect(jsonPath("$[?(@.creditCardNumber == 1234567812345678)]").exists());
    }

    @Test
    public void testGetUsers_WithCreditCardFalse() throws Exception {
        User user1 = new User(1L, "johndoe", "Password1", "johndoe@example.org", LocalDate.of(2003, 1, 1), 1234567812345678L);
        User user2 = new User(2L, "janedoe", "Password1", "janedoe@example.org", LocalDate.of(2003, 1, 1), null);
        
        doNothing().when(userService).registerUser(user1);

        mockMvc.perform(post("/users/register")
            .contentType("application/json")
            .content(oMapper.writeValueAsString(user1)))
            .andExpect(status().isCreated())
            .andExpect(content().string("User registered successfully"));

        doNothing().when(userService).registerUser(user2);

        mockMvc.perform(post("/users/register")
            .contentType("application/json")
            .content(oMapper.writeValueAsString(user2)))
            .andExpect(status().isCreated())
            .andExpect(content().string("User registered successfully"));

        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);

        when(userService.getUsers(Optional.of(false))).thenReturn(users);

        mockMvc.perform(get("/users?hasCreditCard=no"))
            .andExpect(status().is(200))
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.size()").value(1))
            .andExpect(jsonPath("$[?(@.id == 2)]").exists())
            .andExpect(jsonPath("$[?(@.username == 'janedoe')]").exists())
            .andExpect(jsonPath("$[?(@.email == 'janedoe@example.org')]").exists())
            .andExpect(jsonPath("$[?(@.dateOfBirth == '2003-01-01')]").exists())
            .andExpect(jsonPath("$[?(@.creditCardNumber == null)]").exists());
    }

    @Test
    public void testGetUsers_InvalidParameter() throws Exception {
        mockMvc.perform(get("/users?hasCreditCard=maybe"))
            .andExpect(status().is(400))
            .andExpect(content().string("[]"));
    }
}
