package com.example.auth_server.security;

import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

/**
 * FASE 2 - PASSO 3: Customiza JWT com claims do Open Finance
 */
public class JwtTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    @Override
    public void customize(JwtEncodingContext context) {

        // Só customiza access_token
        if ("access_token".equals(context.getTokenType().getValue())) {

            // Extrai scope
            String scope = context.getAuthorizedScopes().toString();

            // Extrai consent ID do scope
            String consentId = extractConsentId(scope);

            // Adiciona claims customizadas
            if (consentId != null) {
                context.getClaims().claim("consent_id", consentId);
            }

            context.getClaims()
                    .claim("organization_id", "banco-organizacao-123")
                    .claim("software_id", "tpp-software-456");

            // Adiciona CPF do usuário (se disponível)
            if (context.getPrincipal().getName() != null) {
                context.getClaims().claim("cpf", getCpfFromUsername(context.getPrincipal().getName()));
            }
        }

        // Para ID Token (OpenID Connect)
        if ("id_token".equals(context.getTokenType().getValue())) {
            context.getClaims()
                    .claim("cpf", getCpfFromUsername(context.getPrincipal().getName()))
                    .claim("account_holder", true);
        }
    }

    /**
     * Extrai consent ID do scope
     */
    private String extractConsentId(String scope) {
        if (scope == null)
            return null;

        // Remove colchetes e espaços extras
        scope = scope.replaceAll("[\\[\\]]", "").trim();

        for (String s : scope.split(",")) {
            s = s.trim();
            if (s.startsWith("consent:")) {
                return s.substring(8);
            }
        }
        return null;
    }

    /**
     * Obtém CPF do username
     * Em produção: buscar do banco de dados
     */
    private String getCpfFromUsername(String username) {
        // Simula busca no banco
        if ("joao.silva".equals(username)) {
            return "12345678900";
        }
        return "00000000000";
    }
}