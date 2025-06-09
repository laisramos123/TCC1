package com.example.auth_server.data;

import java.util.Set;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.auth_server.model.Client;
import com.example.auth_server.model.User;
import com.example.auth_server.repository.ClientRepository;
import com.example.auth_server.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ClientDataInitializer implements ApplicationRunner {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(ClientDataInitializer.class);

    public ClientDataInitializer(ClientRepository clientRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("🚀 Iniciando criação de dados de teste...");

        // Verificar se o repositório está funcionando
        long userCount = userRepository.count();
        long clientCount = clientRepository.count();

        logger.info("📊 Usuários existentes: {}", userCount);
        logger.info("📊 Clientes existentes: {}", clientCount);

        createTestUsers();
        createTestClient();

        // Verificar novamente após criação
        userCount = userRepository.count();
        clientCount = clientRepository.count();

        logger.info("✅ Total de usuários após criação: {}", userCount);
        logger.info("✅ Total de clientes após criação: {}", clientCount);
    }

    private void createTestUsers() {
        try {
            if (userRepository.findByUsername("user").isEmpty()) {
                User user = new User();
                user.setUsername("user");
                String encodedPassword = passwordEncoder.encode("password");
                user.setPassword(encodedPassword);
                user.setName("Test User");
                user.setEmail("user@example.com");
                user.setEnabled(true);
                user.setAuthorities(Set.of("ROLE_USER"));

                User savedUser = userRepository.save(user);
                logger.info("✅ Usuário 'user' criado com ID: {} | Senha: {}",
                        savedUser.getId(), encodedPassword);
            } else {
                User existingUser = userRepository.findByUsername("user").get();
                logger.info("ℹ️ Usuário 'user' já existe com ID: {}", existingUser.getId());
            }

            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                String encodedPassword = passwordEncoder.encode("password");
                admin.setPassword(encodedPassword);
                admin.setName("Admin User");
                admin.setEmail("admin@example.com");
                admin.setEnabled(true);
                admin.setAuthorities(Set.of("ROLE_USER", "ROLE_ADMIN"));

                User savedAdmin = userRepository.save(admin);
                logger.info("✅ Usuário 'admin' criado com ID: {}", savedAdmin.getId());
            }
        } catch (Exception e) {
            logger.error("❌ Erro ao criar usuários: {}", e.getMessage(), e);
        }
    }

    private void createTestClient() {
        try {
            if (clientRepository.findByClientId("oauth-client").isEmpty()) {
                Client client = new Client();
                client.setClientId("oauth-client");
                client.setClientSecret(passwordEncoder.encode("oauth-client-secret"));
                client.setClientName("TPP OAuth Client");
                client.setRedirectUris(Set.of(
                        "http://localhost:8081/login/oauth2/code/tpp-client",
                        "http://localhost:8081/authorized"));
                client.setScopes(Set.of(
                        "openid", "profile", "email",
                        "accounts", "credit-cards-accounts", "loans"));
                client.setGrantTypes(Set.of(
                        "authorization_code", "refresh_token"));

                Client savedClient = clientRepository.save(client);
                logger.info("✅ Cliente OAuth criado com ID: {}", savedClient.getId());
            }
        } catch (Exception e) {
            logger.error("❌ Erro ao criar cliente: {}", e.getMessage(), e);
        }
    }
}