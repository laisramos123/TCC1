package com.example.auth_server.config;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

import com.example.auth_server.model.Consent;
import com.example.auth_server.service.ConsentService;

@Component
public class TokenEnhancer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    private final ConsentService consentService;

    public TokenEnhancer(ConsentService consentService) {
        this.consentService = consentService;
    }

    @Override
    public void customize(JwtEncodingContext context) {
        String username = context.getPrincipal().getName();
        String clientId = context.getRegisteredClient().getClientId();

        System.out.println("🎫 Personalizando token JWT:");
        System.out.println("  - Usuário: " + username);
        System.out.println("  - Cliente: " + clientId);
        System.out.println("  - Token Type: " + context.getTokenType().getValue());

        // ✅ Informações básicas do token
        context.getClaims().claim("client_id", clientId);
        context.getClaims().claim("username", username);
        context.getClaims().claim("iss", "http://localhost:8080");

        // ✅ Timestamp de criação
        context.getClaims().claim("iat", Instant.now().getEpochSecond());

        // ✅ Buscar consentimentos do usuário usando o método correto
        List<Consent> userConsents = consentService.findUserConsents(username);
        System.out.println("  - Total de consentimentos encontrados: " + userConsents.size());

        // ✅ Filtrar consentimentos ativos para este cliente
        List<Consent> activeConsents = userConsents.stream()
                .filter(consent -> clientId.equals(consent.getClientId()))
                .filter(consent -> ConsentService.STATUS_AUTHORIZED.equals(consent.getStatus()))
                .filter(consent -> consentService.isConsentValid(consent))
                .collect(Collectors.toList());

        System.out.println("  - Consentimentos ativos para este cliente: " + activeConsents.size());

        if (!activeConsents.isEmpty()) {
            // ✅ Usar o consentimento mais recente
            Consent latestConsent = activeConsents.stream()
                    .max((c1, c2) -> c1.getCreatedAt().compareTo(c2.getCreatedAt()))
                    .orElse(null);

            if (latestConsent != null) {
                System.out.println("  - Usando consentimento: " + latestConsent.getConsentId());

                // ✅ Adicionar informações do consentimento ao token
                context.getClaims().claim("consent_id", latestConsent.getConsentId());
                context.getClaims().claim("permissions", latestConsent.getPermissions());

                // ✅ Adicionar timestamps do consentimento
                if (latestConsent.getCreatedAt() != null) {
                    context.getClaims().claim("consent_created_at",
                            latestConsent.getCreatedAt().toInstant(ZoneOffset.UTC).getEpochSecond());
                }

                if (latestConsent.getExpiresAt() != null) {
                    context.getClaims().claim("consent_expires_at",
                            latestConsent.getExpiresAt().toInstant(ZoneOffset.UTC).getEpochSecond());
                }

                // ✅ Adicionar informações específicas do Open Finance Brasil
                addOpenFinanceClaims(context, latestConsent);

                // ✅ Adicionar scopes baseados nas permissões
                addScopeBasedClaims(context, latestConsent.getPermissions());

                System.out.println("  - Permissões adicionadas ao token: " + latestConsent.getPermissions());
            }
        } else {
            System.out.println("  - ⚠️ Nenhum consentimento ativo encontrado para o cliente");

            // ✅ Adicionar claims mínimos mesmo sem consentimento
            context.getClaims().claim("consent_status", "no_active_consent");
        }

        // ✅ Adicionar informações do contexto da autorização
        if (context.getAuthorizationGrantType() != null) {
            context.getClaims().claim("grant_type", context.getAuthorizationGrantType().getValue());
        }

        // ✅ Adicionar informações do registered client
        if (context.getRegisteredClient() != null) {
            context.getClaims().claim("client_name", context.getRegisteredClient().getClientName());
        }

        // ✅ Adicionar identificador único do token
        context.getClaims().claim("jti", java.util.UUID.randomUUID().toString());

        // ✅ Adicionar informações de compliance do Open Finance
        addComplianceClaims(context);

        System.out.println("✅ Token personalizado com sucesso");
    }

    /**
     * Adiciona claims específicos do Open Finance Brasil
     */
    private void addOpenFinanceClaims(JwtEncodingContext context, Consent consent) {
        System.out.println("🏦 Adicionando claims do Open Finance Brasil");

        // Identificador do participante (sua instituição)
        context.getClaims().claim("org_id", "your-org-id");
        context.getClaims().claim("org_name", "Your Bank Name");

        // Versão da API do Open Finance
        context.getClaims().claim("openbanking_api_version", "2.0.1");

        // Informações de compliance
        context.getClaims().claim("regulatory_environment", "production"); // ou "sandbox"

        // Categorizar permissões por tipo de dado
        Set<String> permissions = consent.getPermissions();

        if (permissions.contains("accounts")) {
            context.getClaims().claim("has_account_access", true);
        }

        if (permissions.contains("credit-cards-accounts")) {
            context.getClaims().claim("has_credit_card_access", true);
        }

        if (permissions.stream().anyMatch(p -> p.contains("transactions"))) {
            context.getClaims().claim("has_transaction_access", true);
        }

        // Adicionar informações de auditoria
        context.getClaims().claim("consent_channel", "web");
        context.getClaims().claim("consent_method", "explicit_approval");
    }

    /**
     * Adiciona claims baseados nos scopes/permissões
     */
    private void addScopeBasedClaims(JwtEncodingContext context, Set<String> permissions) {
        System.out.println("🔐 Adicionando claims baseados em permissões");

        // Informações de perfil
        if (permissions.contains("profile")) {
            context.getClaims().claim("profile_access", true);
        }

        if (permissions.contains("email")) {
            context.getClaims().claim("email_access", true);
        }

        // Determinar nível de acesso
        String accessLevel = determineAccessLevel(permissions);
        context.getClaims().claim("access_level", accessLevel);

        // Adicionar lista de recursos acessíveis
        Set<String> accessibleResources = mapPermissionsToResources(permissions);
        context.getClaims().claim("accessible_resources", accessibleResources);
    }

    /**
     * Determina o nível de acesso baseado nas permissões
     */
    private String determineAccessLevel(Set<String> permissions) {
        if (permissions.contains("accounts") && permissions.contains("credit-cards-accounts")) {
            return "full_banking";
        } else if (permissions.contains("accounts")) {
            return "account_info";
        } else if (permissions.contains("profile") || permissions.contains("email")) {
            return "basic_profile";
        } else {
            return "minimal";
        }
    }

    /**
     * Mapeia permissões para recursos específicos
     */
    private Set<String> mapPermissionsToResources(Set<String> permissions) {
        return permissions.stream()
                .map(this::mapPermissionToResource)
                .collect(Collectors.toSet());
    }

    /**
     * Mapeia uma permissão individual para um recurso
     */
    private String mapPermissionToResource(String permission) {
        switch (permission) {
            case "accounts":
                return "/accounts";
            case "credit-cards-accounts":
                return "/credit-cards-accounts";
            case "loans":
                return "/loans";
            case "financings":
                return "/financings";
            case "invoice-financings":
                return "/invoice-financings";
            case "unarranged-accounts-overdraft":
                return "/unarranged-accounts-overdraft";
            case "profile":
                return "/customer/personal/identifications";
            case "email":
                return "/customer/personal/contacts";
            default:
                return "/resources/" + permission;
        }
    }

    /**
     * Adiciona claims de compliance e auditoria
     */
    private void addComplianceClaims(JwtEncodingContext context) {
        System.out.println("📋 Adicionando claims de compliance");

        // Timestamp para auditoria
        context.getClaims().claim("token_issued_at", Instant.now().toString());

        // Informações de segurança
        context.getClaims().claim("security_profile", "FAPI-R");
        context.getClaims().claim("token_endpoint_auth_method", "client_secret_basic");

        // Informações de compliance LGPD
        context.getClaims().claim("lgpd_compliant", true);
        context.getClaims().claim("data_sharing_agreement", "v2.0");

        // Identificador da sessão de autorização
        context.getClaims().claim("auth_session_id", java.util.UUID.randomUUID().toString());
    }
}
