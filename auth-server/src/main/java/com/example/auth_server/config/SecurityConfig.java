package com.example.auth_server.config;

import com.example.auth_server.security.ConsentAwareAuthorizationProvider;
import com.example.auth_server.security.ConsentValidationFilter;
import com.example.auth_server.service.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationContext;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationException;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

        private final CustomUserDetailsService customUserDetailsService;
        private final ConsentAwareAuthorizationProvider consentProvider;

        // Prefixos de scopes dinâmicos permitidos pelo Open Finance Brasil
        private static final Set<String> DYNAMIC_SCOPE_PREFIXES = Set.of(
                        "consent:",
                        "customer:",
                        "payment:");

        public SecurityConfig(CustomUserDetailsService customUserDetailsService,
                        ConsentAwareAuthorizationProvider consentProvider) {
                this.customUserDetailsService = customUserDetailsService;
                this.consentProvider = consentProvider;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        @Order(1)
        public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
                OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

                // Configura o validador customizado de scopes para Open Finance
                http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                                .authorizationEndpoint(authorizationEndpoint -> authorizationEndpoint
                                                .authenticationProviders(configureAuthenticationProviders()));

                // Adiciona filtro de validação de consent
                http.addFilterBefore(
                                new ConsentValidationFilter(consentProvider),
                                AuthorizationFilter.class);

                http.exceptionHandling((exceptions) -> exceptions
                                .defaultAuthenticationEntryPointFor(
                                                new LoginUrlAuthenticationEntryPoint("/login"),
                                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)));

                return http.build();
        }

        /**
         * Configura os AuthenticationProviders para usar validação customizada de
         * scopes
         */
        private Consumer<List<AuthenticationProvider>> configureAuthenticationProviders() {
                return authenticationProviders -> {
                        for (AuthenticationProvider provider : authenticationProviders) {
                                if (provider instanceof OAuth2AuthorizationCodeRequestAuthenticationProvider authProvider) {
                                        authProvider.setAuthenticationValidator(openFinanceScopeValidator());
                                        logger.info("✅ OpenFinance Scope Validator configurado com sucesso!");
                                        System.out.println("✅ OpenFinance Scope Validator configurado!");
                                }
                        }
                };
        }

        /**
         * Validador customizado que aceita scopes dinâmicos do Open Finance Brasil
         * Exemplos de scopes dinâmicos aceitos:
         * - consent:urn:bancoabc:C1DD93123
         * - customer:cpf:12345678901
         * - payment:urn:pagamento:P123
         */
        private Consumer<OAuth2AuthorizationCodeRequestAuthenticationContext> openFinanceScopeValidator() {
                return context -> {
                        OAuth2AuthorizationCodeRequestAuthenticationToken authRequest = context.getAuthentication();
                        RegisteredClient registeredClient = context.getRegisteredClient();

                        Set<String> requestedScopes = authRequest.getScopes();
                        Set<String> allowedScopes = registeredClient.getScopes();

                        // Log detalhado para debug
                        System.out.println("========================================");
                        System.out.println("🔍 VALIDAÇÃO DE SCOPES OPEN FINANCE");
                        System.out.println("   Scopes solicitados: " + requestedScopes);
                        System.out.println("   Scopes permitidos (base): " + allowedScopes);
                        System.out.println("   Prefixos dinâmicos aceitos: " + DYNAMIC_SCOPE_PREFIXES);
                        System.out.println("========================================");

                        logger.debug("Validando scopes - Solicitados: {}, Permitidos: {}", requestedScopes,
                                        allowedScopes);

                        for (String requestedScope : requestedScopes) {
                                boolean isAllowed = isScopeAllowed(requestedScope, allowedScopes);

                                if (!isAllowed) {
                                        System.out.println("❌ Scope REJEITADO: " + requestedScope);
                                        logger.warn("Scope rejeitado: {}", requestedScope);

                                        OAuth2Error error = new OAuth2Error(
                                                        OAuth2ErrorCodes.INVALID_SCOPE,
                                                        "Scope não permitido: " + requestedScope,
                                                        "https://datatracker.ietf.org/doc/html/rfc6749#section-4.1.2.1");
                                        throw new OAuth2AuthorizationCodeRequestAuthenticationException(error, null);
                                }

                                System.out.println("✅ Scope ACEITO: " + requestedScope);
                                logger.debug("Scope aceito: {}", requestedScope);
                        }

                        System.out.println("========================================");
                        System.out.println("✅ TODOS OS SCOPES VALIDADOS COM SUCESSO!");
                        System.out.println("========================================");
                };
        }

        /**
         * Verifica se um scope é permitido
         * 
         * @param requestedScope O scope solicitado
         * @param allowedScopes  Os scopes registrados para o cliente
         * @return true se o scope é permitido
         */
        private boolean isScopeAllowed(String requestedScope, Set<String> allowedScopes) {
                // 1. Verifica se é um scope estático registrado (ex: openid, accounts, profile)
                if (allowedScopes.contains(requestedScope)) {
                        System.out.println("   → Scope estático encontrado: " + requestedScope);
                        return true;
                }

                // 2. Verifica se é um scope dinâmico com prefixo permitido (ex:
                // consent:uuid-123)
                for (String prefix : DYNAMIC_SCOPE_PREFIXES) {
                        if (requestedScope.startsWith(prefix)) {
                                // Extrai o scope base (ex: "consent" de "consent:")
                                String baseScope = prefix.substring(0, prefix.length() - 1);

                                // Verifica se o scope base está registrado no cliente
                                if (allowedScopes.contains(baseScope)) {
                                        System.out.println("   → Scope dinâmico permitido: " + requestedScope
                                                        + " (base: " + baseScope + ")");
                                        return true;
                                }

                                // Se o prefixo está na lista de permitidos, aceita mesmo sem o base registrado
                                // Isso permite flexibilidade para Open Finance
                                System.out.println("   → Scope dinâmico permitido pelo prefixo: " + requestedScope);
                                return true;
                        }
                }

                System.out.println("   → Scope NÃO encontrado: " + requestedScope);
                return false;
        }

        @Bean
        public DaoAuthenticationProvider daoAuthenticationProvider() {
                DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
                provider.setUserDetailsService(customUserDetailsService);
                provider.setPasswordEncoder(passwordEncoder());
                return provider;
        }

        @Bean
        @Order(2)
        public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
                http
                                .userDetailsService(customUserDetailsService)
                                .authenticationProvider(daoAuthenticationProvider())
                                .authorizeHttpRequests((authorize) -> authorize
                                                // Endpoints públicos
                                                .requestMatchers(
                                                                "/login",
                                                                "/error",
                                                                "/css/**",
                                                                "/js/**",
                                                                "/images/**",
                                                                "/favicon.ico")
                                                .permitAll()

                                                // ✅ CORRIGIDO: Actuator endpoints públicos para Prometheus
                                                .requestMatchers(
                                                                "/actuator/**",
                                                                "/actuator/health",
                                                                "/actuator/health/**",
                                                                "/actuator/prometheus",
                                                                "/actuator/info",
                                                                "/actuator/metrics",
                                                                "/actuator/metrics/**")
                                                .permitAll()

                                                // Open Banking API pública
                                                .requestMatchers("/open-banking/**").permitAll()

                                                // APIs de Dilithium e Benchmark públicas
                                                .requestMatchers("/api/v1/dilithium/**").permitAll()
                                                .requestMatchers("/api/v1/benchmark/**").permitAll()

                                                // ✅ JWKS e metadata OAuth2 devem ser públicos
                                                .requestMatchers("/.well-known/**").permitAll()
                                                .requestMatchers("/oauth2/jwks").permitAll()

                                                // Qualquer outra requisição precisa de autenticação
                                                .anyRequest().authenticated())

                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .failureUrl("/login?error")
                                                .permitAll())

                                .logout(logout -> logout
                                                .logoutSuccessUrl("/login?logout")
                                                .permitAll())

                                // ✅ CORRIGIDO: CSRF desabilitado para endpoints necessários
                                .csrf(csrf -> csrf
                                                .ignoringRequestMatchers(
                                                                "/open-banking/**",
                                                                "/api/v1/dilithium/**",
                                                                "/api/v1/benchmark/**",
                                                                "/actuator/**",
                                                                "/oauth2/token",
                                                                "/oauth2/introspect",
                                                                "/oauth2/revoke"));

                return http.build();
        }
}