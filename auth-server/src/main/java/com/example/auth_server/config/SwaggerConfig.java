package com.example.auth_server.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SwaggerConfig {

        @Value("${spring.application.name}")
        private String applicationName;

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Open Finance  Authorization Server API")
                                                .version("v1.0")
                                                .description("Authorization Server com suporte a criptografia pós-quântica (Dilithium)")
                                                .termsOfService("https://openbanking.org.br/terms")
                                                .contact(new Contact()
                                                                .name("TCC UnB Team")
                                                                .email("tcc@unb.br")
                                                                .url("https://github.com/tcc-unb"))
                                                .license(new License()
                                                                .name("Apache 2.0")
                                                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                                .servers(getServers())
                                .addSecurityItem(new SecurityRequirement().addList("oauth2"))
                                .components(new Components()
                                                .addSecuritySchemes("oauth2", createOAuth2Scheme()));
        }

        private List<Server> getServers() {
                return Arrays.asList(
                                new Server().url("http://localhost:8080").description("Desenvolvimento - RSA"),
                                new Server().url("http://localhost:9080").description("Desenvolvimento - Dilithium"),
                                new Server().url("http://localhost:8080").description("Docker - Interno"));
        }

        private SecurityScheme createOAuth2Scheme() {
                return new SecurityScheme()
                                .type(SecurityScheme.Type.OAUTH2)
                                .description("OAuth2 com suporte a Dilithium para Open Finance ")
                                .flows(new OAuthFlows()
                                                .authorizationCode(new OAuthFlow()
                                                                .authorizationUrl("/oauth2/authorize")
                                                                .tokenUrl("/oauth2/token")
                                                                .scopes(new Scopes()
                                                                                .addString("openid", "OpenID Connect")
                                                                                .addString("accounts",
                                                                                                "Leitura de contas")
                                                                                .addString("transactions",
                                                                                                "Leitura de transações")
                                                                                .addString("credit-cards-accounts",
                                                                                                "Leitura de cartões"))));
        }

        @Bean
        public GroupedOpenApi consentApi() {
                return GroupedOpenApi.builder()
                                .group("consent-api")
                                .displayName("Consent API")
                                .pathsToMatch("/open-banking/consents/**")
                                .build();
        }

        @Bean
        public GroupedOpenApi dilithiumApi() {
                return GroupedOpenApi.builder()
                                .group("dilithium-api")
                                .displayName("Dilithium Signature API")
                                .pathsToMatch("/api/v1/dilithium/**")
                                .build();
        }

        @Bean
        public GroupedOpenApi oauth2Api() {
                return GroupedOpenApi.builder()
                                .group("oauth2-api")
                                .displayName("OAuth2/OIDC API")
                                .pathsToMatch("/oauth2/**", "/.well-known/**")
                                .build();
        }
}