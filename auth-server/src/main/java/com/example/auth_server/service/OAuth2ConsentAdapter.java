package com.example.auth_server.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import com.example.auth_server.model.Consent;

public class OAuth2ConsentAdapter implements OAuth2AuthorizationConsentService {

    private final ConsentService consentService;
    private final JdbcTemplate jdbcTemplate;
    private final RegisteredClientRepository registeredClientRepository;

    public OAuth2ConsentAdapter(ConsentService consentService,
            JdbcTemplate jdbcTemplate,
            RegisteredClientRepository registeredClientRepository) {
        this.consentService = consentService;
        this.jdbcTemplate = jdbcTemplate;
        this.registeredClientRepository = registeredClientRepository;
    }

    @Override
    public void save(OAuth2AuthorizationConsent authorizationConsent) {
        System.out.println(" Salvando OAuth2AuthorizationConsent");

        // Salvar no formato do Spring OAuth2 (tabela oauth2_authorization_consent)
        saveToDB(authorizationConsent);

        // TAMBÉM salvar no seu ConsentService para Open Finance
        String userId = authorizationConsent.getPrincipalName();
        String clientId = authorizationConsent.getRegisteredClientId();

        Set<String> permissions = authorizationConsent.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("SCOPE_"))
                .map(auth -> auth.substring(6))
                .collect(Collectors.toSet());

        // Buscar consentimento pendente
        Consent consent = consentService.findLatestPendingConsent(userId, clientId);
        if (consent != null) {
            System.out.println("✅ Aprovando consentimento existente: " + consent.getConsentId());
            consentService.approveConsent(consent.getConsentId());
        } else {
            System.out.println("⚠️ Nenhum consentimento pendente encontrado - criando novo");
            // Criar novo consentimento se não existir
            try {
                Consent newConsent = consentService.createConsent(userId, clientId, permissions);
                consentService.approveConsent(newConsent.getConsentId());
            } catch (Exception e) {
                System.err.println("❌ Erro ao criar/aprovar consentimento: " + e.getMessage());
            }
        }
    }

    @Override
    public OAuth2AuthorizationConsent findById(String registeredClientId, String principalName) {
        System.out.println("🔍 Buscando OAuth2AuthorizationConsent: " + registeredClientId + " / " + principalName);

        // Primeiro tentar buscar do banco OAuth2
        OAuth2AuthorizationConsent consent = findInDB(registeredClientId, principalName);

        if (consent != null) {
            System.out.println("✅ Encontrado no banco OAuth2");
            return consent;
        }

        // Se não encontrar, verificar no ConsentService
        List<Consent> userConsents = consentService.findUserConsents(principalName);

        Consent activeConsent = userConsents.stream()
                .filter(c -> c.getClientId().equals(registeredClientId)) // ✅ Mudança: getClientId()
                .filter(c -> ConsentService.STATUS_AUTHORIZED.equals(c.getStatus()))
                .filter(c -> consentService.isConsentValid(c))
                .findFirst()
                .orElse(null);

        if (activeConsent == null) {
            System.out.println("❌ Nenhum consentimento ativo encontrado");
            return null;
        }

        System.out.println("✅ Convertendo consentimento para OAuth2AuthorizationConsent");

        // Converter para OAuth2AuthorizationConsent
        Set<GrantedAuthority> authorities = activeConsent.getPermissions().stream()
                .map(permission -> (GrantedAuthority) () -> "SCOPE_" + permission)
                .collect(Collectors.toSet());

        return OAuth2AuthorizationConsent.withId(registeredClientId, principalName)
                .authorities(authConsent -> authConsent.addAll(authorities))
                .build();
    }

    @Override
    public void remove(OAuth2AuthorizationConsent authorizationConsent) {
        System.out.println("🗑️ Removendo OAuth2AuthorizationConsent");

        // Remover do banco OAuth2
        removeFromDB(authorizationConsent);

        // Revogar no ConsentService
        String userId = authorizationConsent.getPrincipalName();
        String clientId = authorizationConsent.getRegisteredClientId();

        List<Consent> consents = consentService.findUserConsents(userId).stream()
                .filter(c -> c.getClientId().equals(clientId)) // ✅ Mudança: getClientId()
                .filter(c -> ConsentService.STATUS_AUTHORIZED.equals(c.getStatus()))
                .collect(Collectors.toList());

        consents.forEach(c -> {
            try {
                consentService.revokeConsent(c.getConsentId());
                System.out.println("🚫 Consentimento revogado: " + c.getConsentId());
            } catch (Exception e) {
                System.err.println("❌ Erro ao revogar consentimento " + c.getConsentId() + ": " + e.getMessage());
            }
        });
    }

    // Métodos auxiliares para interagir com banco OAuth2
    private void saveToDB(OAuth2AuthorizationConsent consent) {
        try {
            String authorities = consent.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));

            // Usar MERGE do H2 em vez de MySQL ON DUPLICATE KEY UPDATE
            String sql = """
                    MERGE INTO oauth2_authorization_consent
                    (registered_client_id, principal_name, authorities)
                    VALUES (?, ?, ?)
                    """;

            jdbcTemplate.update(sql,
                    consent.getRegisteredClientId(),
                    consent.getPrincipalName(),
                    authorities);

            System.out.println("💾 OAuth2AuthorizationConsent salvo no banco");
        } catch (Exception e) {
            System.err.println("❌ Erro ao salvar OAuth2AuthorizationConsent: " + e.getMessage());
        }
    }

    private OAuth2AuthorizationConsent findInDB(String clientId, String principalName) {
        try {
            String sql = """
                    SELECT registered_client_id, principal_name, authorities
                    FROM oauth2_authorization_consent
                    WHERE registered_client_id = ? AND principal_name = ?
                    """;

            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, clientId, principalName);

            if (results.isEmpty()) {
                return null;
            }

            Map<String, Object> row = results.get(0);
            String authoritiesStr = (String) row.get("authorities");

            Set<GrantedAuthority> authorities = Arrays.stream(authoritiesStr.split(","))
                    .map(auth -> (GrantedAuthority) () -> auth)
                    .collect(Collectors.toSet());

            return OAuth2AuthorizationConsent.withId(clientId, principalName)
                    .authorities(authConsent -> authConsent.addAll(authorities))
                    .build();

        } catch (Exception e) {
            System.err.println("❌ Erro ao buscar OAuth2AuthorizationConsent: " + e.getMessage());
            return null;
        }
    }

    private void removeFromDB(OAuth2AuthorizationConsent consent) {
        try {
            String sql = """
                    DELETE FROM oauth2_authorization_consent
                    WHERE registered_client_id = ? AND principal_name = ?
                    """;

            jdbcTemplate.update(sql,
                    consent.getRegisteredClientId(),
                    consent.getPrincipalName());

            System.out.println("🗑️ OAuth2AuthorizationConsent removido do banco");
        } catch (Exception e) {
            System.err.println("❌ Erro ao remover OAuth2AuthorizationConsent: " + e.getMessage());
        }
    }
}