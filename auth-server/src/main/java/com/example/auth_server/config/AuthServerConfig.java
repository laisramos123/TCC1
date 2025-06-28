package com.example.auth_server.config;

import java.security.Signature;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtEncodingException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationContext;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2AccessTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2RefreshTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import com.example.auth_server.dilithium.DilithiumJWKSource;
import com.example.auth_server.dilithium.DilithiumKeyGeneratorService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.jca.JCAContext;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@Slf4j
public class AuthServerConfig {

    private final DilithiumJWKSource dilithiumJWKSource;

    public AuthServerConfig(DilithiumJWKSource dilithiumJWKSource) {
        this.dilithiumJWKSource = dilithiumJWKSource;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder) {
        JdbcRegisteredClientRepository repository = new JdbcRegisteredClientRepository(jdbcTemplate);

        try {
            if (repository.findByClientId("oauth-client") == null) {
                RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
                        .clientId("oauth-client")
                        .clientSecret(passwordEncoder.encode("oauth-client-secret"))
                        .clientName("TPP OAuth Client - Quantum Secure")
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                        .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                        .redirectUri("http://localhost:8081/login/oauth2/code/tpp-client")
                        .scope(OidcScopes.OPENID)
                        .scope(OidcScopes.PROFILE)
                        .scope(OidcScopes.EMAIL)
                        .scope("accounts")
                        .scope("transactions")
                        .clientSettings(ClientSettings.builder()
                                .requireAuthorizationConsent(true)
                                .requireProofKey(false)
                                .build())
                        .tokenSettings(TokenSettings.builder()
                                .accessTokenTimeToLive(Duration.ofHours(1))
                                .refreshTokenTimeToLive(Duration.ofDays(7))
                                .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                                .reuseRefreshTokens(false)
                                .build())
                        .build();

                repository.save(client);
                log.info(" Cliente OAuth quantum-secure criado: oauth-client");
            }
        } catch (Exception e) {
            log.warn(" Cliente será criado posteriormente: {}", e.getMessage());
        }

        return repository;
    }

    @Bean
    public OAuth2AuthorizationService authorizationService(JdbcTemplate jdbcTemplate,
            RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
    }

    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService(JdbcTemplate jdbcTemplate,
            RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository);
    }

    @Bean
    @Primary
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.builder()
                .username("user")
                .password(passwordEncoder.encode("password")) // ← USAR BCrypt
                .roles("USER")
                .build();

        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin")) // ← USAR BCrypt
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(user, admin);
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new QuantumSecureJwtEncoder(dilithiumJWKSource);
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return new QuantumSecureJwtDecoder(dilithiumJWKSource);
    }

    @Bean
    public OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator() {
        JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder());

        jwtGenerator.setJwtCustomizer(jwtTokenContext -> {
            JwtClaimsSet.Builder claimsBuilder = jwtTokenContext.getClaims();

            claimsBuilder
                    .claim("quantum_secure", true)
                    .claim("crypto_algorithm", "Dilithium3")
                    .claim("security_level", "post-quantum");

            try {
                String keyId = dilithiumJWKSource.getCurrentDilithiumJWK().getKeyID();
                claimsBuilder.claim("kid", keyId);
            } catch (Exception e) {
                log.debug("Não foi possível obter Key ID para token", e);
            }
        });

        OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
        OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();

        return new DelegatingOAuth2TokenGenerator(
                jwtGenerator, accessTokenGenerator, refreshTokenGenerator);
    }

    @Bean
    public Function<OidcUserInfoAuthenticationContext, OidcUserInfo> userInfoMapper() {
        return context -> {
            Map<String, Object> userInfo = new HashMap<>();

            OAuth2Authorization authorization = context.getAuthorization();

            String subject = authorization.getPrincipalName();
            userInfo.put("sub", subject);
            userInfo.put("name", subject);
            userInfo.put("preferred_username", subject);

            userInfo.put("quantum_secure", true);
            userInfo.put("crypto_algorithm", "Dilithium3");
            userInfo.put("security_level", "post-quantum");

            try {
                userInfo.put("key_id", dilithiumJWKSource.getCurrentDilithiumJWK().getKeyID());
            } catch (Exception e) {
                log.debug("Não foi possível obter Key ID para UserInfo", e);
            }

            Set<String> scopes = authorization.getAuthorizedScopes();
            if (scopes.contains("profile")) {
                userInfo.put("family_name", "Quantum");
                userInfo.put("given_name", "User");
            }

            if (scopes.contains("email")) {
                userInfo.put("email", subject + "@quantum-secure.local");
                userInfo.put("email_verified", true);
            }

            return new OidcUserInfo(userInfo);
        };
    }

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .oidc(oidc -> oidc
                        .userInfoEndpoint(userInfo -> userInfo
                                .userInfoMapper(userInfoMapper())))
                .authorizationEndpoint(authz -> authz
                        .consentPage("/oauth2/consent"));

        http.exceptionHandling(exceptions -> exceptions
                .defaultAuthenticationEntryPointFor(
                        new LoginUrlAuthenticationEntryPoint("/login"),
                        new MediaTypeRequestMatcher(MediaType.TEXT_HTML)));

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/h2-console/**", "/debug/**").permitAll()
                        .requestMatchers("/login", "/api/quantum/**").permitAll()
                        .requestMatchers("/.well-known/**").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll())
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**", "/api/quantum/**"))
                .headers(headers -> headers
                        .frameOptions().sameOrigin());

        return http.build();
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("http://localhost:8080")
                .build();
    }

    public static class QuantumSecureJwtEncoder implements JwtEncoder {
        private final DilithiumJWKSource jwkSource;

        public QuantumSecureJwtEncoder(DilithiumJWKSource jwkSource) {
            this.jwkSource = jwkSource;
        }

        @Override
        public Jwt encode(JwtEncoderParameters parameters) throws JwtEncodingException {
            try {
                DilithiumKeyGeneratorService.DilithiumJWK dilithiumJWK = jwkSource.getCurrentDilithiumJWK();

                JwtClaimsSet claims = parameters.getClaims();
                JwsHeader headers = parameters.getJwsHeader() != null ? parameters.getJwsHeader()
                        : JwsHeader.with(SignatureAlgorithm.RS256).build();

                JWSHeader.Builder headerBuilder = new JWSHeader.Builder(JWSAlgorithm.parse("Dilithium3"))
                        .keyID(dilithiumJWK.getKeyID())
                        .type(JOSEObjectType.JWT);

                JWSHeader jwsHeader = headerBuilder.build();

                JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder();
                claims.getClaims().forEach((key, value) -> {
                    if (value instanceof Date) {
                        claimsBuilder.claim(key, (Date) value);
                    } else if (value instanceof Instant) {
                        claimsBuilder.claim(key, Date.from((Instant) value));
                    } else {
                        claimsBuilder.claim(key, value);
                    }
                });

                claimsBuilder.claim("alg", "Dilithium3");
                claimsBuilder.claim("quantum_secure", true);

                JWTClaimsSet jwtClaimsSet = claimsBuilder.build();

                SignedJWT signedJWT = new SignedJWT(jwsHeader, jwtClaimsSet);

                Signature signer = Signature.getInstance("Dilithium", "BCPQC");
                signer.initSign(dilithiumJWK.getPrivateKey());

                byte[] signingInput = signedJWT.getSigningInput();
                signer.update(signingInput);
                byte[] signature = signer.sign();

                signedJWT.sign(new DilithiumJWSSigner(signature));

                return createJwt(signedJWT);

            } catch (Exception e) {
                log.error("Erro ao codificar JWT com Dilithium", e);
                throw new JwtEncodingException("Erro ao codificar JWT com Dilithium", e);
            }
        }

        private Jwt createJwt(SignedJWT signedJWT) throws Exception {
            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

            Map<String, Object> headers = signedJWT.getHeader().toJSONObject();
            Map<String, Object> claims = claimsSet.toJSONObject();

            Instant issuedAt = claimsSet.getIssueTime() != null ? claimsSet.getIssueTime().toInstant() : Instant.now();
            Instant expiresAt = claimsSet.getExpirationTime() != null ? claimsSet.getExpirationTime().toInstant()
                    : Instant.now().plusSeconds(3600);

            return new Jwt(
                    signedJWT.serialize(),
                    issuedAt,
                    expiresAt,
                    headers,
                    claims);
        }
    }

    public static class QuantumSecureJwtDecoder implements JwtDecoder {
        private final DilithiumJWKSource jwkSource;

        public QuantumSecureJwtDecoder(DilithiumJWKSource jwkSource) {
            this.jwkSource = jwkSource;
        }

        @Override
        public Jwt decode(String token) throws JwtException {
            try {
                SignedJWT signedJWT = SignedJWT.parse(token);

                String keyId = signedJWT.getHeader().getKeyID();
                if (keyId == null) {
                    throw new JwtException("Token não possui Key ID");
                }

                DilithiumKeyGeneratorService.DilithiumJWK dilithiumJWK = jwkSource.getDilithiumJWKById(keyId)
                        .orElse(jwkSource.getCurrentDilithiumJWK());

                if (!verifyDilithiumSignature(signedJWT, dilithiumJWK)) {
                    throw new JwtException("Assinatura Dilithium inválida");
                }

                return createJwt(signedJWT);

            } catch (Exception e) {
                log.error("Erro ao decodificar JWT Dilithium", e);
                throw new JwtException("Erro ao decodificar JWT Dilithium", e);
            }
        }

        private boolean verifyDilithiumSignature(SignedJWT signedJWT,
                DilithiumKeyGeneratorService.DilithiumJWK jwk) {
            try {
                Signature verifier = Signature.getInstance("Dilithium", "BCPQC");
                verifier.initVerify(jwk.getPublicKey());

                byte[] signingInput = signedJWT.getSigningInput();
                verifier.update(signingInput);

                byte[] signature = signedJWT.getSignature().decode();

                return verifier.verify(signature);

            } catch (Exception e) {
                log.error("Erro na verificação da assinatura Dilithium", e);
                return false;
            }
        }

        private Jwt createJwt(SignedJWT signedJWT) throws Exception {
            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

            Map<String, Object> headers = signedJWT.getHeader().toJSONObject();
            Map<String, Object> claims = claimsSet.toJSONObject();

            Instant issuedAt = claimsSet.getIssueTime() != null ? claimsSet.getIssueTime().toInstant() : Instant.now();
            Instant expiresAt = claimsSet.getExpirationTime() != null ? claimsSet.getExpirationTime().toInstant()
                    : Instant.now().plusSeconds(3600);

            return new Jwt(
                    signedJWT.serialize(),
                    issuedAt,
                    expiresAt,
                    headers,
                    claims);
        }
    }

    private static class DilithiumJWSSigner implements JWSSigner {
        private final byte[] signature;

        public DilithiumJWSSigner(byte[] signature) {
            this.signature = signature;
        }

        @Override
        public Base64URL sign(JWSHeader header, byte[] signingInput) throws JOSEException {
            return Base64URL.encode(signature);
        }

        @Override
        public Set<JWSAlgorithm> supportedJWSAlgorithms() {
            return Set.of(JWSAlgorithm.parse("Dilithium3"));
        }

        @Override
        public JCAContext getJCAContext() {
            throw new UnsupportedOperationException("Unimplemented method 'getJCAContext'");
        }
    }
}