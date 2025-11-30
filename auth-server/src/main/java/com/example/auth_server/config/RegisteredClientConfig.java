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

                        try {
                                RegisteredClient existingClient = clientRepository.findByClientId(clientId);

                                if (existingClient != null) {
                                        System.out.println("========================================");
                                        System.out.println("✅ OAuth2 Client já existe: " + clientId);
                                        System.out.println("   ID: " + existingClient.getId());
                                        System.out.println("   Scopes: " + existingClient.getScopes());
                                        System.out.println("   Redirect URIs: " + existingClient.getRedirectUris());
                                        System.out.println("========================================");
                                        return;
                                }

                                RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
                                                .clientId(clientId)
                                                .clientSecret("{noop}secret") // {noop} para senha em texto plano
                                                .clientName("OAuth Client Application")

                                                .clientAuthenticationMethod(
                                                                ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                                .clientAuthenticationMethod(
                                                                ClientAuthenticationMethod.CLIENT_SECRET_POST)

                                                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)

                                                .redirectUri("http://localhost:8081/callback")
                                                .redirectUri("http://localhost:8081/authorized")

                                                .postLogoutRedirectUri("http://localhost:8081")

                                                // ✅ SCOPES - incluindo todos necessários para Open Finance
                                                .scope(OidcScopes.OPENID)
                                                .scope(OidcScopes.PROFILE)
                                                .scope("accounts")
                                                .scope("consent") // Base para consent:* dinâmico
                                                .scope("credit-cards-accounts")
                                                .scope("customers")
                                                .scope("resources")
                                                .scope("payments")
                                                .scope("customer") // Base para customer:* dinâmico
                                                .scope("payment") // Base para payment:* dinâmico

                                                .clientSettings(ClientSettings.builder()
                                                                .requireProofKey(true) // PKCE obrigatório
                                                                .requireAuthorizationConsent(false) // Não pedir consent
                                                                                                    // na UI do AS
                                                                .build())

                                                .tokenSettings(TokenSettings.builder()
                                                                .accessTokenTimeToLive(Duration.ofMinutes(30))
                                                                .refreshTokenTimeToLive(Duration.ofDays(7))
                                                                .authorizationCodeTimeToLive(Duration.ofMinutes(5))
                                                                .reuseRefreshTokens(false)
                                                                .build())

                                                .build();

                                clientRepository.save(client);

                                // Verifica se foi salvo
                                RegisteredClient saved = clientRepository.findByClientId(clientId);
                                if (saved != null) {
                                        System.out.println("========================================");
                                        System.out.println("✅ OAuth2 Client CRIADO E PERSISTIDO!");
                                        System.out.println("========================================");
                                        System.out.println("   Client ID: " + clientId);
                                        System.out.println("   Client Secret: secret");
                                        System.out.println("   Redirect URIs:");
                                        System.out.println("     - http://localhost:8081/callback");
                                        System.out.println("     - http://localhost:8081/authorized");
                                        System.out.println("   Scopes:");
                                        saved.getScopes().forEach(scope -> System.out.println("     - " + scope));
                                        System.out.println("   PKCE: obrigatório");
                                        System.out.println("========================================");
                                } else {
                                        System.err.println(
                                                        "❌ ERRO: Cliente foi salvo mas não encontrado na verificação!");
                                }

                        } catch (Exception e) {
                                System.err.println("========================================");
                                System.err.println("❌ ERRO AO CRIAR/VERIFICAR CLIENTE OAUTH2");
                                System.err.println("========================================");
                                System.err.println("   Erro: " + e.getMessage());
                                System.err.println("   Causa: "
                                                + (e.getCause() != null ? e.getCause().getMessage() : "N/A"));
                                e.printStackTrace();
                                System.err.println("========================================");
                                System.err.println("ℹ️  O cliente pode ser criado via SQL se necessário.");
                                System.err.println("========================================");
                        }
                };
        }
}