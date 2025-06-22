package com.example.auth_server.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.auth_server.dilithium.DilithiumJwtService;
import com.example.auth_server.repository.UserRepository;
import com.example.auth_server.service.ConsentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/debug")
@RequiredArgsConstructor
public class BeanStatusController {
    private final ApplicationContext applicationContext;

    @GetMapping("/beans")
    public Map<String, Object> checkBeans() {
        Map<String, Object> status = new HashMap<>();

        try {
            // Verificar beans críticos
            status.put("userRepository", checkBean(UserRepository.class));
            status.put("registeredClientRepository", checkBean(RegisteredClientRepository.class));
            status.put("consentService", checkBean(ConsentService.class));
            status.put("dilithiumJwtService", checkBean(DilithiumJwtService.class));

            // Contar usuários
            UserRepository userRepo = applicationContext.getBean(UserRepository.class);
            status.put("userCount", userRepo.count());

            // Verificar cliente OAuth2
            RegisteredClientRepository clientRepo = applicationContext.getBean(RegisteredClientRepository.class);
            boolean clientExists = clientRepo.findByClientId("oauth-client") != null;
            status.put("oauthClientExists", clientExists);

            // Status geral
            status.put("allBeansLoaded", true);
            status.put("timestamp", java.time.LocalDateTime.now());

        } catch (Exception e) {
            status.put("error", e.getMessage());
            status.put("allBeansLoaded", false);
        }

        return status;
    }

    private boolean checkBean(Class<?> beanClass) {
        try {
            applicationContext.getBean(beanClass);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @GetMapping("/status")
    public Map<String, Object> simpleStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "OK");
        status.put("timestamp", java.time.LocalDateTime.now());
        status.put("message", "Authorization Server is running");
        return status;
    }
}
