package com.example.auth_server.controller;

import java.util.Map;
import java.util.HashMap;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.auth_server.model.User;
import com.example.auth_server.repository.UserRepository;

@RestController
@RequestMapping("/debug")
public class DebugController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DebugController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/test-password")
    public Map<String, Object> testPassword() {
        try {
            User user = userRepository.findByUsername("user").orElse(null);
            if (user == null) {
                return Map.of(
                        "error", "User not found",
                        "message", "Usuário 'user' não foi encontrado no banco de dados");
            }

            boolean matches = passwordEncoder.matches("password", user.getPassword());

            Map<String, Object> result = new HashMap<>();
            result.put("username", user.getUsername());
            result.put("storedPassword", user.getPassword());
            result.put("testPassword", "password");
            result.put("passwordMatches", matches);
            result.put("enabled", user.isEnabled());
            result.put("authorities", user.getAuthorities());
            result.put("userId", user.getId());
            result.put("email", user.getEmail());
            result.put("name", user.getName());

            return result;

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Exception occurred");
            error.put("message", e.getMessage());
            error.put("type", e.getClass().getSimpleName());
            return error;
        }
    }

    @GetMapping("/users")
    public Map<String, Object> listUsers() {
        try {
            long userCount = userRepository.count();
            return Map.of(
                    "totalUsers", userCount,
                    "users", userRepository.findAll());
        } catch (Exception e) {
            return Map.of(
                    "error", "Exception occurred",
                    "message", e.getMessage());
        }
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "OK",
                "timestamp", java.time.LocalDateTime.now(),
                "message", "Authorization Server Debug Endpoint");
    }
}