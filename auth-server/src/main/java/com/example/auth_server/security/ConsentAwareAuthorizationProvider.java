package com.example.auth_server.security;

import org.springframework.stereotype.Component;

import com.example.auth_server.enums.ConsentStatus;
import com.example.auth_server.service.ConsentService;

@Component
public class ConsentAwareAuthorizationProvider {

    private final ConsentService consentService;

    public ConsentAwareAuthorizationProvider(ConsentService consentService) {
        this.consentService = consentService;
    }

    public void validateConsentBeforeAuthorization(String scope) {

        String consentId = extractConsentId(scope);

        if (consentId == null) {
            throw new RuntimeException("Consent ID não encontrado no scope");
        }

        consentService.validateConsentForAuthorization(consentId);
    }

    public void updateConsentAfterAuthorization(String scope) {

        String consentId = extractConsentId(scope);

        if (consentId != null) {
            consentService.updateStatus(consentId, ConsentStatus.AUTHORISED);
        }
    }

    public void markConsentAsRejected(String scope, String reason) {

        String consentId = extractConsentId(scope);

        if (consentId != null) {
            consentService.updateStatus(consentId, ConsentStatus.REJECTED);
        }
    }

    private String extractConsentId(String scope) {
        if (scope == null) {
            return null;
        }

        for (String s : scope.split(" ")) {
            if (s.startsWith("consent:")) {
                return s.substring(8);
            }
        }

        return null;
    }
}
