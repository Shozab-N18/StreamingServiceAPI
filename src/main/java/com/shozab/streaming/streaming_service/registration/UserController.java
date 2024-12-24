package com.shozab.streaming.streaming_service.registration;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.ValidationException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequestMapping(path = "/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        try {
            userService.registerUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully"); // 201
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // 400
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage()); // 403
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()); // 409
        }
    }
    
    @GetMapping
	public ResponseEntity<List<User>> getUsers(@RequestParam Optional<String> hasCreditCard) {
		if (hasCreditCard.isPresent()) {
            String param = hasCreditCard.get().trim().toLowerCase();
            
            switch (param) {
                case "yes":
                    return ResponseEntity.ok(userService.getUsers(Optional.of(true)));
                case "no":
                    return ResponseEntity.ok(userService.getUsers(Optional.of(false)));
                default:
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.emptyList());
            }
        }
        return ResponseEntity.ok(userService.getUsers(Optional.empty()));
	}
}