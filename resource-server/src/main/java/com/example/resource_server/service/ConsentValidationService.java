package com.example.resource_server.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.resource_server.dto.ConsentResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * FASE 3 - PASSO 2: Validação de consentimento no Resource Server
 * Chama Consent API do Authorization Server
 */
@Service
public class ConsentValidationService {

    @Value("${authorization-server.base-url}")
    private String authServerUrl;

    private final RestTemplate restTemplate;

    public ConsentValidationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Valida consentimento para acesso aos recursos
     */
    public void validateConsentForResourceAccess(String consentId, String requiredPermission) {

        if (consentId == null) {
            throw new RuntimeException("Consent ID não encontrado no token");
        }

        String url = authServerUrl + "/open-banking/consents/v2/consents/" + consentId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-fapi-interaction-id", UUID.randomUUID().toString());

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<ConsentResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    ConsentResponse.class);

            ConsentResponse consent = response.getBody();

            if (consent == null || consent.getData() == null) {
                throw new RuntimeException("Consentimento não encontrado");
            }

            // Valida status
            if (!"AUTHORISED".equals(consent.getData().getStatus())) {
                throw new RuntimeException(
                        "Consentimento não está autorizado. Status: " + consent.getData().getStatus());
            }

            // Valida expiração
            LocalDateTime expiration = consent.getData().getExpirationDateTime();
            if (expiration.isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Consentimento expirado");
            }

            // Valida permissão específica
            List<String> permissions = consent.getData().getPermissions();
            if (requiredPermission != null && !permissions.contains(requiredPermission)) {
                throw new RuntimeException(
                        "Consentimento não possui a permissão: " + requiredPermission);
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao validar consentimento: " + e.getMessage());
        }
    }
}