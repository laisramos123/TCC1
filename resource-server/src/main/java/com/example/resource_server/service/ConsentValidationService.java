package com.example.resource_server.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.resource_server.dto.ConsentResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ConsentValidationService {

    @Value("${authorization-server.base-url}")
    private String authServerUrl;

    private final RestTemplate restTemplate;

    public ConsentValidationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

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

            if (!"AUTHORISED".equals(consent.getData().getStatus())) {
                throw new RuntimeException(
                        "Consentimento não está autorizado. Status: " + consent.getData().getStatus());
            }

            LocalDateTime expiration = consent.getData().getExpirationDateTime();
            if (expiration.isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Consentimento expirado");
            }

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