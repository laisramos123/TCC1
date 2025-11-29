package com.example.auth_server.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;
import java.util.UUID;

@Configuration
public class RegisteredClientConfig {

        @Bean
        public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
                return new JdbcRegisteredClientRepository(jdbcTemplate);
        }

        @Bean
        public CommandLineRunner initializeOAuth2Client(
                        RegisteredClientRepository clientRepository,
                        PasswordEncoder passwordEncoder) {

                return args -> {
                        String clientId = "oauth-client";

                        RegisteredClient existingClient = clientRepository.findByClientId(clientId);

                        if (existingClient != null) {
                                System.out.println("✅ OAuth2 Client já existe: " + clientId);
                                System.out.println("   Scopes: " + existingClient.getScopes());
                                return;
                        }

                        RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
                                        .clientId(clientId)
                                        .clientSecret("{noop}secret")
                                        .clientName("OAuth Client Application")

                                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)

                                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                        .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)

                                        .redirectUri("http://localhost:8081/callback")
                                        .redirectUri("http://localhost:8081/authorized")

                                        .postLogoutRedirectUri("http://localhost:8081")

                                        .scope(OidcScopes.OPENID)
                                        .scope(OidcScopes.PROFILE)
                                        .scope("accounts")
                                        .scope("consent")
                                        .scope("credit-cards-accounts")
                                        .scope("customers")
                                        .scope("resources")
                                        .scope("payments")

                                        .clientSettings(ClientSettings.builder()
                                                        .requireProofKey(true)
                                                        .requireAuthorizationConsent(false)
                                                        .build())

                                        .tokenSettings(TokenSettings.builder()
                                                        .accessTokenTimeToLive(Duration.ofMinutes(30))
                                                        .refreshTokenTimeToLive(Duration.ofDays(7))
                                                        .authorizationCodeTimeToLive(Duration.ofMinutes(5))
                                                        .reuseRefreshTokens(false)
                                                        .build())

                                        .build();

                        clientRepository.save(client);

                        System.out.println("========================================");
                        System.out.println("✅ OAuth2 Client criado com sucesso!");
                        System.out.println("========================================");
                        System.out.println("   Client ID: " + clientId);
                        System.out.println("   Client Secret: secret");
                        System.out.println("   Redirect URIs:");
                        System.out.println("     - http://localhost:8081/callback");
                        System.out.println("     - http://localhost:8081/authorized");
                        System.out.println("   Scopes:");
                        System.out.println("     - openid");
                        System.out.println("     - profile");
                        System.out.println("     - accounts");
                        System.out.println("     - consent (permite consent:*)");
                        System.out.println("     - credit-cards-accounts");
                        System.out.println("     - customers");
                        System.out.println("     - resources");
                        System.out.println("     - payments");
                        System.out.println("   PKCE: obrigatório");
                        System.out.println("========================================");
                };
        }

}