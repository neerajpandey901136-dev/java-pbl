package com.expense.tracker.controllers;

import com.expense.tracker.models.User;
import com.expense.tracker.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username already exists"));
        }
        // In a real app, hash the password! For simple college project, storing as is.
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(Map.of(
                "message", "User registered successfully",
                "userId", savedUser.getId()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "userId", userOpt.get().getId(),
                    "username", userOpt.get().getUsername()
            ));
        }
        return ResponseEntity.status(401).body(Map.of("message", "Invalid credentials"));
    }
}
