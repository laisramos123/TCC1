package com.example.auth_server.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.stereotype.Service;

import com.example.auth_server.model.ConsentRecord;
import com.example.auth_server.repository.ConsentRepository;

@Service
public class ConsentService {

    private final ConsentRepository consentRepository;

    @Autowired
    public ConsentService(ConsentRepository consentRepository) {
        this.consentRepository = consentRepository;
    }

    public String registerConsent(String userId, String clientId, String scope) {
        // Cria um novo registro de consentimento
        ConsentRecord consent = new ConsentRecord();
        consent.setId(UUID.randomUUID().toString());
        consent.setUserId(userId);
        consent.setClientId(clientId);
        consent.setScopes(Arrays.asList(scope.split(" ")));
        consent.setCreatedAt(LocalDateTime.now());
        consent.setExpiresAt(LocalDateTime.now().plusDays(90)); // Prazo de 90 dias (padrão Open Finance)
        consent.setStatus("ACTIVE");

        // Salva o consentimento
        consentRepository.save(consent);
        return consent.getId();
    }

    public Map<String, String> getScopeDescriptions(Set<String> scopes) {
        Map<String, String> descriptions = new HashMap<>();

        // Descreve cada escopo de forma amigável para o usuário
        for (String scope : scopes) {
            switch (scope) {
                case OidcScopes.OPENID:
                    descriptions.put(scope, "Acesso à sua identidade básica");
                    break;
                case OidcScopes.PROFILE:
                    descriptions.put(scope, "Acesso ao seu perfil (nome, foto, etc)");
                    break;
                case "accounts":
                    descriptions.put(scope, "Visualizar informações das suas contas (saldo, extrato, etc)");
                    break;
                case "payments":
                    descriptions.put(scope, "Iniciar pagamentos em seu nome");
                    break;
                default:
                    descriptions.put(scope, "Acesso ao escopo: " + scope);
            }
        }

        return descriptions;
    }

    public ConsentRecord getActiveConsent(String userId, String clientId) {
        return consentRepository.findActiveConsent(userId, clientId);
    }

    public void revokeConsent(String consentId) {
        ConsentRecord consent = consentRepository.findById(consentId)
                .orElseThrow(() -> new IllegalArgumentException("Consentimento não encontrado"));

        consent.setStatus("REVOKED");
        consent.setRevokedAt(LocalDateTime.now());
        consentRepository.save(consent);
    }
}
