package com.example.auth_server.config;

import com.example.auth_server.security.X509AuthenticationFilter;
import com.example.auth_server.service.CustomOAuth2UserService;
import com.example.auth_server.service.CustomUserDetailsService;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.UUID;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired(required = false)
    @Lazy
    private X509AuthenticationFilter x509AuthenticationFilter;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // @Bean
    // @Order(1)
    // public SecurityFilterChain
    // authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
    // OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

    // http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
    // .oidc(Customizer.withDefaults());

    // http
    // .exceptionHandling((exceptions) -> exceptions
    // .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
    // )
    // .oauth2ResourceServer((resourceServer) -> resourceServer
    // .jwt(Customizer.withDefaults())
    // );

    // return http.build();
    // }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http,
            @Lazy AuthenticationManager authenticationManager) throws Exception {
        if (x509AuthenticationFilter != null) {
            x509AuthenticationFilter.setAuthenticationManager(authenticationManager);
        }

        http
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(
                                "/actuator/health",
                                "/login",
                                "/error",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**",
                                "/open-banking/consents/**")
                        .permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll())
                .csrf(AbstractHttpConfigurer::disable);

        if (x509AuthenticationFilter != null) {
            http.addFilterBefore(x509AuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        }

        return http.build();
    }

    // @Bean
    // public RegisteredClientRepository registeredClientRepository() {
    // RegisteredClient registeredClient =
    // RegisteredClient.withId(UUID.randomUUID().toString())
    // .clientId("client")
    // .clientSecret(passwordEncoder().encode("secret"))
    // .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
    // .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
    // .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
    // .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
    // .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
    // .redirectUri("http://localhost:8081/login/oauth2/code/auth-server")
    // .redirectUri("http://localhost:8081/authorized")
    // .scope(OidcScopes.OPENID)
    // .scope(OidcScopes.PROFILE)
    // .scope("read")
    // .scope("write")
    // .clientSettings(ClientSettings.builder()
    // .requireAuthorizationConsent(true)
    // .requireProofKey(true)
    // .build())
    // .tokenSettings(TokenSettings.builder()
    // .accessTokenTimeToLive(Duration.ofMinutes(15))
    // .refreshTokenTimeToLive(Duration.ofDays(1))
    // .build())
    // .build();

    // return new InMemoryRegisteredClientRepository(registeredClient);
    // }

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

    private static KeyPair generateRsaKey() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    // @Bean
    // public AuthorizationServerSettings authorizationServerSettings() {
    // return AuthorizationServerSettings.builder()
    // .issuer("http://localhost:8080")
    // .build();
    // }
}
