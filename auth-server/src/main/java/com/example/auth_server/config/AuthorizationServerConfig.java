package com.example.auth_server.config;

import com.example.auth_server.jwt.CustomJwtEncoder;
import com.example.auth_server.security.ConsentAwareAuthorizationProvider;
import com.example.auth_server.security.ConsentValidationFilter;
import com.example.auth_server.security.JwtTokenCustomizer;
import com.example.auth_server.signature.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.Duration;

@Configuration
public class AuthorizationServerConfig {

    @Value("${jwt.signature.algorithm:RSA}")
    private String algorithmName;

    @Value("${jwt.signature.issuer:https://localhost:8080}")
    private String issuer;

    private final SignatureAlgorithm rsaSignature;
    private final SignatureAlgorithm dilithiumSignature;

    public AuthorizationServerConfig(
            @Qualifier("rsaSignature") SignatureAlgorithm rsaSignature,
            @Qualifier("dilithiumSignature") SignatureAlgorithm dilithiumSignature) {

        this.rsaSignature = rsaSignature;
        this.dilithiumSignature = dilithiumSignature;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(
            HttpSecurity http,
            ConsentAwareAuthorizationProvider consentProvider) throws Exception {

        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .oidc(Customizer.withDefaults());

        http
                .addFilterBefore(
                        new ConsentValidationFilter(consentProvider),
                        UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling((exceptions) -> exceptions
                        .authenticationEntryPoint(
                                new LoginUrlAuthenticationEntryPoint("/login")));

        return http.build();
    }

    @Bean
    public JwtEncoder jwtEncoder() throws Exception {
        SignatureAlgorithm algorithm = getActiveAlgorithm();

        if (algorithm.getPublicKey() == null) {
            algorithm.generateKeyPair();
        }

        return new CustomJwtEncoder(algorithm, issuer);
    }

    @Bean
    public TokenSettings tokenSettings() {
        return TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(30))
                .refreshTokenTimeToLive(Duration.ofDays(30))
                .reuseRefreshTokens(false)
                .build();
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer(issuer)
                .build();
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return new JwtTokenCustomizer();
    }

    private SignatureAlgorithm getActiveAlgorithm() {
        return "DILITHIUM".equalsIgnoreCase(algorithmName)
                ? dilithiumSignature
                : rsaSignature;
    }
}