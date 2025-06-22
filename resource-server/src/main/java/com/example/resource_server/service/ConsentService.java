package com.example.resource_server.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.resource_server.exception.ConsentException;
import com.example.resource_server.model.Consent;
import com.example.resource_server.repository.ConsentRepository;

@Service
public class ConsentService {

    private final ConsentRepository consentRepository;

    public ConsentService(ConsentRepository consentRepository) {
        this.consentRepository = consentRepository;
    }

    /**
     * Verifica se o consentimento é válido para o acesso solicitado
     * 
     * @param jwt          Token JWT do usuário
     * @param resourceType Tipo de recurso sendo acessado (accounts, transactions,
     *                     etc)
     * @return true se o consentimento for válido
     */
    @Transactional(readOnly = true)
    public boolean validateConsent(Jwt jwt, String resourceType) {
        // Verifica se o escopo apropriado está presente no token
        List<String> scopes = jwt.getClaimAsStringList("scope");
        if (scopes == null || !scopes.contains(resourceType)) {
            return false;
        }

        // Obtém o ID do consentimento e o clientId do token
        String consentId = jwt.getClaimAsString("consent_id");
        String clientId = jwt.getClaimAsString("client_id");

        if (consentId == null || clientId == null) {
            return false;
        }

        // Busca o consentimento no banco de dados
        Optional<Consent> consentOpt = consentRepository.findByConsentId(consentId);

        if (consentOpt.isEmpty()) {
            return false;
        }

        Consent consent = consentOpt.get();

        // Verifica se o consentimento:
        // 1. Está autorizado
        // 2. Não expirou
        // 3. Não foi revogado
        // 4. Foi concedido para o cliente correto
        // 5. Inclui a permissão para o recurso solicitado

        return "AUTHORIZED".equals(consent.getStatus()) &&
                consent.getExpiresAt().isAfter(LocalDateTime.now()) &&
                consent.getRevokedAt() == null &&
                consent.getClientId().equals(clientId) &&
                consent.getPermissions().contains(resourceType);
    }

    /**
     * Obtém o ID do consentimento do token JWT
     */
    public Optional<String> getConsentId(Jwt jwt) {
        return Optional.ofNullable(jwt.getClaimAsString("consent_id"));
    }

    /**
     * Cria um novo consentimento
     */
    @Transactional
    public Consent createConsent(String userId, String clientId, Set<String> permissions) {
        Consent consent = new Consent();
        consent.setConsentId(generateConsentId());
        consent.setUserId(userId);
        consent.setClientId(clientId);
        consent.setPermissions(permissions);
        consent.setCreatedAt(LocalDateTime.now());
        consent.setExpiresAt(LocalDateTime.now().plusDays(90)); // Consentimento válido por 90 dias
        consent.setStatus("AWAITING_AUTHORIZATION");

        return consentRepository.save(consent);
    }

    /**
     * Autoriza um consentimento
     */
    @Transactional
    public Consent authorizeConsent(String consentId) {
        Consent consent = consentRepository.findByConsentId(consentId)
                .orElseThrow(() -> new ConsentException("Consentimento não encontrado"));

        consent.setStatus("AUTHORIZED");
        return consentRepository.save(consent);
    }

    /**
     * Revoga um consentimento
     */
    @Transactional
    public Consent revokeConsent(String consentId) {
        Consent consent = consentRepository.findByConsentId(consentId)
                .orElseThrow(() -> new ConsentException("Consentimento não encontrado"));

        consent.setStatus("REVOKED");
        consent.setRevokedAt(LocalDateTime.now());
        return consentRepository.save(consent);
    }

    private String generateConsentId() {
        // Em uma implementação real, usaria um algoritmo mais robusto para gerar IDs
        // únicos
        return "urn:consent:" + java.util.UUID.randomUUID().toString();
    }
}