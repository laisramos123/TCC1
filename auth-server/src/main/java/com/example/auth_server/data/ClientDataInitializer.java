package com.example.auth_server.data;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.Duration;

import java.util.Set;
import java.util.UUID;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
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
    private final RegisteredClientRepository registeredClientRepository;
    private final JdbcTemplate jdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(ClientDataInitializer.class);

    public ClientDataInitializer(ClientRepository clientRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RegisteredClientRepository registeredClientRepository,
            JdbcTemplate jdbcTemplate) {
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.registeredClientRepository = registeredClientRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info(" Iniciando criacao de dados de teste...");

        // Verificar contadores iniciais
        long userCount = userRepository.count();
        long clientCount = clientRepository.count();

        logger.info("Estado inicial - usuarios: {}, Clientes: {}", userCount, clientCount);

        createTestUsers();
        createTestClients();

        // Verificar contadores finais
        userCount = userRepository.count();
        clientCount = clientRepository.count();

        logger.info(" Estado final - Usuarios: {}, Clientes: {}", userCount, clientCount);
    }

    private void createTestUsers() {
        try {
            // Criar usuario padrao
            if (userRepository.findByUsername("user").isEmpty()) {
                User user = new User();
                user.setUsername("user");
                user.setPassword(passwordEncoder.encode("password"));
                user.setName("Test User");
                user.setEmail("user@example.com");
                user.setEnabled(true);
                user.setAuthorities(Set.of("ROLE_USER"));

                User savedUser = userRepository.save(user);
                logger.info(" Usuario 'user' criado com ID: {}", savedUser.getId());
            } else {
                User existingUser = userRepository.findByUsername("user").get();
                logger.info(" Usuario 'user' ja existe com ID: {}", existingUser.getId());
            }

            // Criar usuario admin
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("password"));
                admin.setName("Admin User");
                admin.setEmail("admin@example.com");
                admin.setEnabled(true);
                admin.setAuthorities(Set.of("ROLE_USER", "ROLE_ADMIN"));

                User savedAdmin = userRepository.save(admin);
                logger.info(" Usuario 'admin' criado com ID: {}", savedAdmin.getId());
            } else {
                logger.info(" Usuario 'admin' ja existe");
            }
        } catch (Exception e) {
            logger.error(" Erro ao criar usuarios: {}", e.getMessage(), e);
        }
    }

    private void createTestClients() {
        try {
            String clientId = "oauth-client";
            String clientSecret = "oauth-client-secret";

            // 1. Criar no formato JPA (sua entidade Client)
            createJpaClient(clientId, clientSecret);

            // 2. Criar no formato Spring Authorization Server (oauth2_registered_client)
            createOAuth2RegisteredClient(clientId, clientSecret);

        } catch (Exception e) {
            logger.error(" Erro ao criar clientes: {}", e.getMessage(), e);
        }
    }

    private void createJpaClient(String clientId, String clientSecret) {
        if (clientRepository.findByClientId(clientId).isEmpty()) {
            Client client = new Client();
            client.setClientId(clientId);
            client.setClientSecret(passwordEncoder.encode(clientSecret));
            client.setClientName("TPP OAuth Client");
            client.setRedirectUris(Set.of(
                    "http://localhost:8081/login/oauth2/code/tpp-client",
                    "http://localhost:8081/authorized",
                    "http://localhost:8081/callback"));
            client.setScopes(Set.of(
                    "openid", "profile", "email",
                    "accounts", "credit-cards-accounts", "loans"));
            client.setGrantTypes(Set.of(
                    "authorization_code", "refresh_token"));

            Client savedClient = clientRepository.save(client);
            logger.info(" Cliente JPA criado com ID: {} | Client ID: {}",
                    savedClient.getId(), savedClient.getClientId());
        } else {
            logger.info(" Cliente JPA '{}' ja existe", clientId);
        }
    }

    private void createOAuth2RegisteredClient(String clientId, String clientSecret) {
        // Verificar se ja existe
        RegisteredClient existingClient = registeredClientRepository.findByClientId(clientId);
        if (existingClient != null) {
            logger.info(" RegisteredClient '{}' ja existe com ID: {}", clientId, existingClient.getId());
            return;
        }

        try {
            // Criar RegisteredClient programaticamente
            RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId(clientId)
                    .clientSecret(passwordEncoder.encode(clientSecret))
                    .clientName("TPP OAuth Client")
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    .redirectUri("http://localhost:8081/login/oauth2/code/tpp-client")
                    .redirectUri("http://localhost:8081/authorized")
                    .redirectUri("http://localhost:8081/callback")
                    .scope(OidcScopes.OPENID)
                    .scope(OidcScopes.PROFILE)
                    .scope(OidcScopes.EMAIL)
                    .scope("accounts")
                    .scope("credit-cards-accounts")
                    .scope("loans")
                    .scope("financings")
                    .scope("invoice-financings")
                    .scope("unarranged-accounts-overdraft")
                    .clientSettings(ClientSettings.builder()
                            .requireAuthorizationConsent(true)
                            .requireProofKey(false)
                            .build())
                    .tokenSettings(TokenSettings.builder()
                            .accessTokenTimeToLive(Duration.ofHours(1))
                            .refreshTokenTimeToLive(Duration.ofDays(30))
                            .reuseRefreshTokens(false)
                            .authorizationCodeTimeToLive(Duration.ofMinutes(5))
                            .build())
                    .build();

            registeredClientRepository.save(registeredClient);
            logger.info(" RegisteredClient criado: {} | ID: {}", clientId, registeredClient.getId());

            // Log do secret para desenvolvimento
            logger.info(" Client Secret (desenvolvimento): {}", clientSecret);

        } catch (Exception e) {
            logger.error(" Erro ao criar RegisteredClient: {}", e.getMessage());

            // Fallback: tentar insercao direta via JDBC se necessario
            try {
                createOAuth2ClientViaJdbc(clientId, clientSecret);
            } catch (Exception jdbcError) {
                logger.error(" Erro no fallback JDBC: {}", jdbcError.getMessage());
            }
        }
    }

    private void createOAuth2ClientViaJdbc(String clientId, String clientSecret) {
        String sql = """
                INSERT INTO oauth2_registered_client (
                    id, client_id, client_id_issued_at, client_secret, client_name,
                    client_authentication_methods, authorization_grant_types,
                    redirect_uris, scopes, client_settings, token_settings
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        String id = UUID.randomUUID().toString();
        String encodedSecret = passwordEncoder.encode(clientSecret);
        Timestamp now = Instant.now().toEpochMilli() > 0
                ? Timestamp.from(Instant.now())
                : Timestamp.valueOf("1970-01-01 00:00:00");

        // Settings simplificados para evitar problemas de JSON
        String clientSettings = """
                {"@class":"java.util.Collections$UnmodifiableMap",
                 "settings.client.require-proof-key":false,
                 "settings.client.require-authorization-consent":true}
                """.replaceAll("\\s+", "");

        String tokenSettings = """
                {"@class":"java.util.Collections$UnmodifiableMap",
                 "settings.token.reuse-refresh-tokens":false,
                 "settings.token.access-token-time-to-live":["java.time.Duration",3600.000000000],
                 "settings.token.refresh-token-time-to-live":["java.time.Duration",2592000.000000000]}
                """.replaceAll("\\s+", "");

        jdbcTemplate.update(sql,
                id, // id
                clientId, // client_id
                now, // client_id_issued_at
                encodedSecret, // client_secret
                "TPP OAuth Client", // client_name
                "client_secret_basic,client_secret_post", // client_authentication_methods
                "authorization_code,refresh_token", // authorization_grant_types
                "http://localhost:8081/login/oauth2/code/tpp-client,http://localhost:8081/authorized", // redirect_uris
                "openid,profile,email,accounts,credit-cards-accounts,loans", // scopes
                clientSettings, // client_settings
                tokenSettings // token_settings
        );

        logger.info(" RegisteredClient criado via JDBC: {} | ID: {}", clientId, id);
    }
}