package com.example.auth_server.security;

import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

public class JwtTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    @Override
    public void customize(JwtEncodingContext context) {

        if ("access_token".equals(context.getTokenType().getValue())) {

            String scope = context.getAuthorizedScopes().toString();

            String consentId = extractConsentId(scope);

            if (consentId != null) {
                context.getClaims().claim("consent_id", consentId);
            }

            context.getClaims()
                    .claim("organization_id", "banco-organizacao-123")
                    .claim("software_id", "tpp-software-456");

            if (context.getPrincipal().getName() != null) {
                context.getClaims().claim("cpf", getCpfFromUsername(context.getPrincipal().getName()));
            }
        }

        if ("id_token".equals(context.getTokenType().getValue())) {
            context.getClaims()
                    .claim("cpf", getCpfFromUsername(context.getPrincipal().getName()))
                    .claim("account_holder", true);
        }
    }

    private String extractConsentId(String scope) {
        if (scope == null)
            return null;

        scope = scope.replaceAll("[\\[\\]]", "").trim();

        for (String s : scope.split(",")) {
            s = s.trim();
            if (s.startsWith("consent:")) {
                return s.substring(8);
            }
        }
        return null;
    }

    private String getCpfFromUsername(String username) {

        if ("joao.silva".equals(username)) {
            return "12345678900";
        }
        return "00000000000";
    }
}