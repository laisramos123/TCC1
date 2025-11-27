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

/**
 * Configuração do OAuth2 Registered Client
 * Garante que o client esteja sempre registrado corretamente no banco
 */
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

                        // Verificar se já existe
                        RegisteredClient existingClient = clientRepository.findByClientId(clientId);

                        if (existingClient != null) {
                                System.out.println("✅ OAuth2 Client já existe: " + clientId);
                                return;
                        }

                        // Criar novo client
                        RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
                                        .clientId(clientId)
                                        .clientSecret("{noop}secret") // {noop} = sem criptografia (dev only)
                                        .clientName("OAuth Client Application")

                                        // Métodos de autenticação do client
                                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)

                                        // Grant types permitidos
                                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                        .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)

                                        // URIs de redirecionamento permitidos
                                        .redirectUri("http://localhost:8081/callback")
                                        .redirectUri("http://localhost:8081/authorized")

                                        // URI de logout
                                        .postLogoutRedirectUri("http://localhost:8081")

                                        // Scopes permitidos
                                        .scope("openid")
                                        .scope("profile")
                                        .scope("accounts")
                                        .scope("consents")

                                        // Configurações do cliente
                                        .clientSettings(ClientSettings.builder()
                                                        .requireProofKey(true) // PKCE obrigatório
                                                        .requireAuthorizationConsent(false) // Não exige tela de
                                                                                            // consentimento OAuth2
                                                        .build())

                                        // Configurações de tokens
                                        .tokenSettings(TokenSettings.builder()
                                                        .accessTokenTimeToLive(Duration.ofMinutes(5))
                                                        .refreshTokenTimeToLive(Duration.ofHours(1))
                                                        .authorizationCodeTimeToLive(Duration.ofMinutes(5))
                                                        .reuseRefreshTokens(true)
                                                        .build())

                                        .build();

                        // Salvar no banco
                        clientRepository.save(client);

                        System.out.println("✅ OAuth2 Client criado com sucesso: " + clientId);
                        System.out.println("   Client Secret: secret");
                        System.out.println(
                                        "   Redirect URIs: http://localhost:8081/callback, http://localhost:8081/authorized");
                        System.out.println("   Scopes: openid, profile, accounts, consents");
                        System.out.println("   PKCE: obrigatório");
                };
        }

        @Bean
        public RegisteredClient registeredClient() {
                return RegisteredClient.withId(UUID.randomUUID().toString())
                                .clientId("oauth-client")
                                .clientSecret("{bcrypt}$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW")
                                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                                .redirectUri("http://localhost:8081/callback")
                                .redirectUri("http://localhost:8081/authorized")
                                .postLogoutRedirectUri("http://localhost:8081/logged-out")

                                .scopes(scopes -> {

                                        scopes.add(OidcScopes.OPENID);
                                        scopes.add(OidcScopes.PROFILE);

                                        scopes.add("accounts");
                                        scopes.add("credit-cards-accounts");
                                        scopes.add("customers");
                                        scopes.add("resources");
                                        scopes.add("payments");

                                })

                                .clientSettings(ClientSettings.builder()
                                                .requireProofKey(true)
                                                .requireAuthorizationConsent(false)
                                                .build())
                                .tokenSettings(TokenSettings.builder()
                                                .accessTokenTimeToLive(Duration.ofMinutes(5))
                                                .refreshTokenTimeToLive(Duration.ofDays(1))
                                                .reuseRefreshTokens(false)
                                                .build())
                                .build();
        }
}