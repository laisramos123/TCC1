package com.example.auth_server.dilithium;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

import com.example.auth_server.model.Consent;
import com.example.auth_server.service.ConsentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpenFinanceTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    private final ConsentService consentService;

    @Override
    public void customize(JwtEncodingContext context) {
        String tokenType = context.getTokenType().getValue();
        String username = context.getPrincipal().getName();
        String clientId = context.getRegisteredClient().getClientId();

        log.info("🎫 Customizando token {} para usuário {} e cliente {}",
                tokenType, username, clientId);

        // Adicionar claims padrão
        context.getClaims()
                .claim("iss", "http://localhost:8080") // Emissor
                .claim("client_id", clientId)
                .claim("username", username);

        // Buscar consentimentos ativos
        List<Consent> activeConsents = consentService.findUserConsents(username).stream()
                .filter(c -> c.getClient_id().equals(clientId))
                .filter(c -> "AUTHORISED".equals(c.getStatus()))
                .filter(c -> c.getExpiresAt().isAfter(LocalDateTime.now()))
                .filter(c -> c.getRevokedAt() == null)
                .collect(Collectors.toList());

        if (!activeConsents.isEmpty()) {
            // Usar o consentimento mais recente
            Consent latestConsent = activeConsents.stream()
                    .max((c1, c2) -> c1.getCreatedAt().compareTo(c2.getCreatedAt()))
                    .orElse(null);

            if (latestConsent != null) {
                // Adicionar claims do Open Finance Brasil
                addOpenFinanceClaims(context, latestConsent);
            }
        }

        // Adicionar JTI (JWT ID) único
        context.getClaims().claim("jti", UUID.randomUUID().toString());

        // Adicionar timestamp
        context.getClaims().claim("iat", Instant.now().getEpochSecond());

        log.info("✅ Token customizado com claims do Open Finance");
    }

    private void addOpenFinanceClaims(JwtEncodingContext context, Consent consent) {
        // Claims específicos do Open Finance Brasil
        context.getClaims()
                .claim("consent_id", consent.getConsentId())
                .claim("permissions", consent.getPermissions())
                .claim("consent_status", consent.getStatus())
                .claim("consent_created_at", consent.getCreatedAt().toString())
                .claim("consent_expires_at", consent.getExpiresAt().toString());

        // Identificação da organização (em produção, buscar do registro)
        context.getClaims()
                .claim("org_id", "43e8c7f2-4c4b-4b8a-9452-1c2b3d4e5f6a") // ID fictício
                .claim("org_name", "Banco Digital S.A.")
                .claim("software_id", context.getRegisteredClient().getId())
                .claim("software_statement", "eyJhbGciOiJQUzI1NiIsImtpZCI6I..."); // Em produção, usar o real

        // Informações de segurança
        context.getClaims()
                .claim("auth_time", Instant.now().getEpochSecond())
                .claim("acr", "urn:openbanking:psd2:sca") // Authentication Context Reference
                .claim("amr", List.of("pwd", "mfa")); // Authentication Methods References

        // Dados do recurso compartilhado
        Set<String> permissions = consent.getPermissions();

        // Mapear permissões para recursos acessíveis
        List<String> accessibleResources = permissions.stream()
                .map(this::mapPermissionToResource)
                .collect(Collectors.toList());

        context.getClaims()
                .claim("accessible_resources", accessibleResources)
                .claim("sharing_expires_at", consent.getExpiresAt().toString());

        // Adicionar claims de auditoria
        context.getClaims()
                .claim("data_created_at", Instant.now().toString())
                .claim("request_object_encryption_alg", "RSA-OAEP")
                .claim("request_object_encryption_enc", "A256GCM")
                .claim("request_object_signing_alg", "PS256");

        // Claims específicos por tipo de token
        if ("access_token".equals(context.getTokenType().getValue())) {
            // Para access tokens, adicionar informações de API
            context.getClaims()
                    .claim("api_version", "2.0.0")
                    .claim("api_resources", getApiResources(permissions));
        } else if ("id_token".equals(context.getTokenType().getValue())) {
            // Para ID tokens, adicionar informações do usuário
            context.getClaims()
                    .claim("cpf", maskCpf(getUserCpf(context.getPrincipal().getName())))
                    .claim("name", getUserName(context.getPrincipal().getName()));
        }
    }

    private String mapPermissionToResource(String permission) {
        return switch (permission) {
            case "accounts" -> "/open-banking/accounts/v2";
            case "credit-cards-accounts" -> "/open-banking/credit-cards-accounts/v2";
            case "loans" -> "/open-banking/loans/v2";
            case "financings" -> "/open-banking/financings/v2";
            case "invoice-financings" -> "/open-banking/invoice-financings/v2";
            case "unarranged-accounts-overdraft" -> "/open-banking/unarranged-accounts-overdraft/v2";
            default -> "/open-banking/" + permission + "/v2";
        };
    }

    private List<String> getApiResources(Set<String> permissions) {
        return permissions.stream()
                .flatMap(permission -> {
                    return switch (permission) {
                        case "accounts" -> List.of(
                                "GET /accounts",
                                "GET /accounts/{accountId}",
                                "GET /accounts/{accountId}/balances",
                                "GET /accounts/{accountId}/transactions").stream();
                        case "credit-cards-accounts" -> List.of(
                                "GET /credit-cards-accounts",
                                "GET /credit-cards-accounts/{creditCardAccountId}",
                                "GET /credit-cards-accounts/{creditCardAccountId}/bills",
                                "GET /credit-cards-accounts/{creditCardAccountId}/transactions").stream();
                        default -> List.<String>of().stream();
                    };
                })
                .collect(Collectors.toList());
    }

    private String maskCpf(String cpf) {
        if (cpf == null || cpf.length() < 11)
            return "***.***.***-**";
        return cpf.substring(0, 3) + ".***.***-" + cpf.substring(9);
    }

    private String getUserCpf(String username) {
        // Em produção, buscar do banco de dados
        return "12345678901";
    }

    private String getUserName(String username) {
        // Em produção, buscar do banco de dados
        return "João da Silva";
    }
}