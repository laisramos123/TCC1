package com.example.auth_server.config;

import com.example.auth_server.security.ConsentAwareAuthorizationProvider;
import com.example.auth_server.security.ConsentValidationFilter;
import com.example.auth_server.security.JwtTokenCustomizer;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.UUID;

/**
 * FASE 2: Configuração do Authorization Server
 * COM validação de consentimento integrada
 */
@Configuration
public class AuthorizationServerConfig {

    /**
     * Security filter chain para endpoints OAuth2
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(
            HttpSecurity http,
            ConsentAwareAuthorizationProvider consentProvider) throws Exception {

        // Aplica configuração padrão do Authorization Server
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        // Customiza configuração
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                // Habilita OpenID Connect
                .oidc(Customizer.withDefaults());

        http
                // *** ADICIONA FILTRO DE VALIDAÇÃO DE CONSENTIMENTO ***
                .addFilterBefore(
                        new ConsentValidationFilter(consentProvider),
                        UsernamePasswordAuthenticationFilter.class)

                // Trata exceções
                .exceptionHandling((exceptions) -> exceptions
                        .authenticationEntryPoint(
                                new LoginUrlAuthenticationEntryPoint("/login")))

                // Configura como Resource Server para UserInfo endpoint
                .oauth2ResourceServer((resourceServer) -> resourceServer
                        .jwt(Customizer.withDefaults()));

        return http.build();
    }

    /**
     * Registra clientes OAuth2 (TPPs)
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository() {

        RegisteredClient tppClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("tpp-client-id")
                .clientSecret("{noop}tpp-client-secret") // Em produção: BCrypt

                // Métodos de autenticação
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .clientAuthenticationMethod(ClientAuthenticationMethod.PRIVATE_KEY_JWT)
                .clientAuthenticationMethod(ClientAuthenticationMethod.TLS_CLIENT_AUTH)

                // Grant types
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)

                // Redirect URIs
                .redirectUri("https://tpp.com.br/callback")
                .redirectUri("https://localhost:8080/callback")
                .redirectUri("http://localhost:8080/callback")

                // Scopes
                .scope("openid")
                .scope("profile")
                .scope("email")
                .scope("accounts")
                .scope("credit-cards-accounts")
                .scope("resources")

                // Configurações do cliente
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(true) // Exige consentimento
                        .requireProofKey(true) // PKCE obrigatório
                        .build())

                // Configurações de tokens
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(10))
                        .refreshTokenTimeToLive(Duration.ofDays(60))
                        .reuseRefreshTokens(false)
                        .build())

                .build();

        return new InMemoryRegisteredClientRepository(tppClient);
    }

    /**
     * JWK Set para assinar tokens
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();

        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    /**
     * Decoder JWT
     */
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    /**
     * Configurações do servidor
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("https://auth.banco.com.br")
                .authorizationEndpoint("/oauth2/authorize")
                .tokenEndpoint("/oauth2/token")
                .jwkSetEndpoint("/oauth2/jwks")
                .tokenRevocationEndpoint("/oauth2/revoke")
                .tokenIntrospectionEndpoint("/oauth2/introspect")
                .oidcUserInfoEndpoint("/userinfo")
                .build();
    }

    /**
     * Customizador de tokens
     */
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return new JwtTokenCustomizer();
    }

    /**
     * Gera par de chaves RSA
     */
    private static KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException("Erro ao gerar chaves RSA", ex);
        }
        return keyPair;
    }
}