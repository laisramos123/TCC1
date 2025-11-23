package com.example.auth_server.config;

import java.time.Duration;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

@Configuration
public class RegisteredClientConfig {

        @Bean
        public RegisteredClientRepository registeredClientRepository() {
                RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
                                .clientId("oauth-client")
                                .clientSecret("{noop}oauth-client-secret")
                                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                                .redirectUri("http://localhost:8081/login/oauth2/code/tpp-client")
                                .redirectUri("http://localhost:8081/authorized")
                                .scope(OidcScopes.OPENID)
                                .scope("accounts")
                                .scope("transactions")
                                .scope("credit-cards-accounts")
                                .clientSettings(ClientSettings.builder()
                                                .requireAuthorizationConsent(true)
                                                .requireProofKey(true)
                                                .build())
                                .tokenSettings(TokenSettings.builder()
                                                .accessTokenTimeToLive(Duration.ofMinutes(30))
                                                .refreshTokenTimeToLive(Duration.ofDays(30))
                                                .reuseRefreshTokens(false)
                                                .build())
                                .build();

                return new InMemoryRegisteredClientRepository(client);
        }
}