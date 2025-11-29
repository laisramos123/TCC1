package com.example.auth_server.config;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationContext;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationException;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Consumer;

@Component
public class OpenFinanceScopeValidator implements Consumer<OAuth2AuthorizationCodeRequestAuthenticationContext> {

    private static final Set<String> DYNAMIC_SCOPE_PREFIXES = Set.of(
            "consent:",
            "customer:",
            "payment:");

    @Override
    public void accept(OAuth2AuthorizationCodeRequestAuthenticationContext context) {
        OAuth2AuthorizationCodeRequestAuthenticationToken authRequest = context.getAuthentication();
        RegisteredClient registeredClient = context.getRegisteredClient();

        Set<String> requestedScopes = authRequest.getScopes();
        Set<String> allowedScopes = registeredClient.getScopes();

        System.out.println("========================================");
        System.out.println("🔍 VALIDAÇÃO DE SCOPES OPEN FINANCE");
        System.out.println("   Scopes solicitados: " + requestedScopes);
        System.out.println("   Scopes permitidos: " + allowedScopes);
        System.out.println("========================================");

        for (String requestedScope : requestedScopes) {
            if (!isScopeAllowed(requestedScope, allowedScopes)) {
                System.out.println(" Scope rejeitado: " + requestedScope);
                OAuth2Error error = new OAuth2Error(
                        OAuth2ErrorCodes.INVALID_SCOPE,
                        "Scope não permitido: " + requestedScope,
                        "https://datatracker.ietf.org/doc/html/rfc6749#section-4.1.2.1");
                throw new OAuth2AuthorizationCodeRequestAuthenticationException(error, null);
            }
            System.out.println(" Scope aceito: " + requestedScope);
        }
    }

    private boolean isScopeAllowed(String requestedScope, Set<String> allowedScopes) {

        if (allowedScopes.contains(requestedScope)) {
            return true;
        }

        for (String prefix : DYNAMIC_SCOPE_PREFIXES) {
            if (requestedScope.startsWith(prefix)) {

                String baseScope = prefix.substring(0, prefix.length() - 1);
                return true;
            }
        }

        return false;
    }
}