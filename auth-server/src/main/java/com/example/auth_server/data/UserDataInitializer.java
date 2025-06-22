package com.example.auth_server.data;

import java.util.Set;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.auth_server.model.User;
import com.example.auth_server.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserDataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        log.info("  Inicializando usuarios de teste...");
        createUsers();
        log.info("  Usuarios inicializados");
    }

    private void createUsers() {
        try {
            // Usuario padrao
            if (userRepository.findByUsername("user").isEmpty()) {
                User user = new User();
                user.setUsername("user");
                user.setPassword(passwordEncoder.encode("password"));
                user.setName("Test User");
                user.setEmail("user@example.com");
                user.setEnabled(true);
                user.setAuthorities(Set.of("ROLE_USER"));

                userRepository.save(user);
                log.info("  Usuario 'user' criado");
            } else {
                log.info("  Usuario 'user' ja existe");
            }

            // Usuario admin
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("password"));
                admin.setName("Admin User");
                admin.setEmail("admin@example.com");
                admin.setEnabled(true);
                admin.setAuthorities(Set.of("ROLE_USER", "ROLE_ADMIN"));

                userRepository.save(admin);
                log.info("  Usuario 'admin' criado");
            } else {
                log.info("  Usuario 'admin' ja existe");
            }
        } catch (Exception e) {
            log.error("  Erro ao criar usuarios: {}", e.getMessage());
        }
    }
}