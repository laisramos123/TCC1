package com.example.auth_server.config;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
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
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.example.auth_server.dilithium.DilithiumJwtService;
import com.example.auth_server.dilithium.DilithiumKeyGeneratorService;
import com.example.auth_server.repository.ClientRepository;
import com.example.auth_server.service.ConsentService;
import com.example.auth_server.service.OAuth2ConsentAdapter;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class AuthServerConfig {

    private final ConsentService consentService;
    private final DilithiumJwtService dilithiumJwtService;
    private final DilithiumKeyGeneratorService dilithiumKeyGenerator;

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .oidc(Customizer.withDefaults())
                .authorizationEndpoint(endpoint -> endpoint
                        .consentPage("/oauth2/consent")
                        .authorizationResponseHandler((request, response, authentication) -> {
                            // Criar consentimento no padrão Open Finance
                            log.info("🏦 Processando autorização Open Finance");
                            // Handler customizado será implementado
                        }))
                .tokenEndpoint(endpoint -> endpoint
                        .accessTokenResponseHandler((request, response, authentication) -> {
                            // Log para auditoria Open Finance
                            log.info("🎫 Token emitido para Open Finance");
                        }));

        http
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                (request) -> request.getRequestURI().startsWith("/oauth2")))
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(jwt -> jwt.decoder(jwtDecoder())));

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/api/consents/**").authenticated()
                        .requestMatchers("/api/open-finance/**").authenticated()
                        .requestMatchers("/login", "/error", "/css/**", "/js/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**"))
                .headers(headers -> headers
                        .frameOptions().sameOrigin());

        return http.build();
    }

    /**
     * JWK Source customizado para Dilithium
     * Como o Nimbus JOSE não suporta Dilithium nativamente,
     * precisamos criar uma implementação customizada
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        return (jwkSelector, context) -> {
            // Por enquanto, retornamos um JWKSet vazio
            // Em produção, isso deve ser adaptado para expor as chaves Dilithium
            // em um formato que os clientes possam entender
            return jwkSelector.select(new JWKSet());
        };
    }

    /**
     * JWT Decoder customizado para Dilithium
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        // Implementação customizada que usa DilithiumJwtService
        return token -> {
            try {
                var claims = dilithiumJwtService.verifyAndDecodeJWT(token);

                // Converter para o formato esperado pelo Spring Security
                return new org.springframework.security.oauth2.jwt.Jwt(
                        token,
                        claims.getIssueTime().toInstant(),
                        claims.getExpirationTime().toInstant(),
                        Map.of("alg", "Dilithium3", "typ", "JWT"),
                        claims.getClaims());
            } catch (Exception e) {
                throw new org.springframework.security.oauth2.jwt.JwtException("Falha ao decodificar JWT Dilithium", e);
            }
        };
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("http://localhost:8080")
                .authorizationEndpoint("/oauth2/authorize")
                .tokenEndpoint("/oauth2/token")
                .jwkSetEndpoint("/oauth2/jwks")
                .tokenRevocationEndpoint("/oauth2/revoke")
                .tokenIntrospectionEndpoint("/oauth2/introspect")
                .oidcUserInfoEndpoint("/userinfo")
                .oidcClientRegistrationEndpoint("/oauth2/register")
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configurações de token específicas para Open Finance Brasil
     */
    public static TokenSettings openFinanceTokenSettings() {
        return TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(15)) // Máximo 15 minutos conforme Open Finance
                .refreshTokenTimeToLive(Duration.ofDays(90)) // 90 dias para refresh token
                .reuseRefreshTokens(false) // Não reutilizar refresh tokens
                .authorizationCodeTimeToLive(Duration.ofMinutes(5))
                .build();
    }

    /**
     * Configurações de cliente para TPPs do Open Finance
     */
    public static ClientSettings openFinanceClientSettings() {
        return ClientSettings.builder()
                .requireAuthorizationConsent(true) // Sempre requerer consentimento
                .requireProofKey(true) // PKCE obrigatório
                .build();
    }
}