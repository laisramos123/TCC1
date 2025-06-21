package com.example.auth_server.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;

import com.example.auth_server.model.Consent;

@Component
@Primary
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
            consentService.approveConsent(consent.getConsentId());
        }
    }

    @Override
    public OAuth2AuthorizationConsent findById(String registeredClientId, String principalName) {
        // Primeiro tentar buscar do banco OAuth2
        OAuth2AuthorizationConsent consent = findInDB(registeredClientId, principalName);

        if (consent != null) {
            return consent;
        }

        // Se não encontrar, verificar no ConsentService
        List<Consent> userConsents = consentService.findUserConsents(principalName);

        Consent activeConsent = userConsents.stream()
                .filter(c -> c.getClient_id().equals(registeredClientId))
                .filter(c -> ConsentService.STATUS_AUTHORIZED.equals(c.getStatus()))
                .filter(c -> consentService.isConsentValid(c))
                .findFirst()
                .orElse(null);

        if (activeConsent == null) {
            return null;
        }

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
        // Remover do banco OAuth2
        removeFromDB(authorizationConsent);

        // Revogar no ConsentService
        String userId = authorizationConsent.getPrincipalName();
        String clientId = authorizationConsent.getRegisteredClientId();

        List<Consent> consents = consentService.findUserConsents(userId).stream()
                .filter(c -> c.getClient_id().equals(clientId))
                .filter(c -> ConsentService.STATUS_AUTHORIZED.equals(c.getStatus()))
                .collect(Collectors.toList());

        consents.forEach(c -> consentService.revokeConsent(c.getConsentId()));
    }

    // Métodos auxiliares para interagir com banco OAuth2
    private void saveToDB(OAuth2AuthorizationConsent consent) {
        String authorities = consent.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        jdbcTemplate.update(
                "INSERT INTO oauth2_authorization_consent (registered_client_id, principal_name, authorities) " +
                        "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE authorities = ?",
                consent.getRegisteredClientId(),
                consent.getPrincipalName(),
                authorities,
                authorities);
    }

    private OAuth2AuthorizationConsent findInDB(String clientId, String principalName) {
        // Implementar busca no banco
        // ...
        return null;
    }

    private void removeFromDB(OAuth2AuthorizationConsent consent) {
        jdbcTemplate.update(
                "DELETE FROM oauth2_authorization_consent WHERE registered_client_id = ? AND principal_name = ?",
                consent.getRegisteredClientId(),
                consent.getPrincipalName());
    }
}