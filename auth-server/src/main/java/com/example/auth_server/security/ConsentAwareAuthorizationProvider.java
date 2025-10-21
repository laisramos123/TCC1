package com.example.auth_server.security;

import org.springframework.stereotype.Component;

import com.example.auth_server.enums.ConsentStatus;
import com.example.auth_server.service.ConsentService;

/**
 * FASE 2: Provider que integra Consent API com OAuth2
 */
@Component
public class ConsentAwareAuthorizationProvider {

    private final ConsentService consentService;

    public ConsentAwareAuthorizationProvider(ConsentService consentService) {
        this.consentService = consentService;
    }

    /**
     * PASSO 1: Valida consentimento ANTES de mostrar tela de login
     */
    public void validateConsentBeforeAuthorization(String scope) {

        String consentId = extractConsentId(scope);

        if (consentId == null) {
            throw new RuntimeException("Consent ID não encontrado no scope");
        }

        // Valida via Consent API
        consentService.validateConsentForAuthorization(consentId);
    }

    /**
     * PASSO 2: Atualiza status para AUTHORISED após usuário autorizar
     */
    public void updateConsentAfterAuthorization(String scope) {

        String consentId = extractConsentId(scope);

        if (consentId != null) {
            consentService.updateStatus(consentId, ConsentStatus.AUTHORISED);
        }
    }

    /**
     * PASSO 3: Marca como REJECTED se usuário negar
     */
    public void markConsentAsRejected(String scope, String reason) {

        String consentId = extractConsentId(scope);

        if (consentId != null) {
            consentService.updateStatus(consentId, ConsentStatus.REJECTED);
        }
    }

    /**
     * Extrai consent ID do scope
     * Ex: "openid consent:urn:banco:C123 accounts" → "urn:banco:C123"
     */
    private String extractConsentId(String scope) {
        if (scope == null) {
            return null;
        }

        for (String s : scope.split(" ")) {
            if (s.startsWith("consent:")) {
                return s.substring(8); // Remove "consent:"
            }
        }

        return null;
    }
}
