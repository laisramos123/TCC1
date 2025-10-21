package com.example.auth_client.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.auth_client.dto.ConsentRequest;
import com.example.auth_client.dto.ConsentResponse;

import java.util.List;
import java.util.UUID;

@Service
public class ConsentService {

    @Value("${tpp.bank.authorization-server}")
    private String authorizationServer;

    private final RestTemplate restTemplate;

    public ConsentService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * PASSO 1: Cria consentimento no banco
     */
    public ConsentResponse createConsent(String cpf, List<String> permissions) {

        String url = authorizationServer + "/open-banking/consents/v2/consents";

        ConsentRequest request = ConsentRequest.builder()
                .data(ConsentRequest.Data.builder()
                        .loggedUser(ConsentRequest.LoggedUser.builder()
                                .document(ConsentRequest.Document.builder()
                                        .identification(cpf)
                                        .rel("CPF")
                                        .build())
                                .build())
                        .businessEntity(ConsentRequest.BusinessEntity.builder()
                                .document(ConsentRequest.Document.builder()
                                        .identification(cpf)
                                        .rel("CPF")
                                        .build())
                                .build())
                        .permissions(permissions)
                        .expirationDateTime(java.time.LocalDateTime.now().plusDays(60))
                        .transactionFromDateTime(java.time.LocalDateTime.now().minusYears(1))
                        .transactionToDateTime(java.time.LocalDateTime.now())
                        .build())
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-fapi-interaction-id", UUID.randomUUID().toString());

        HttpEntity<ConsentRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ConsentResponse> response = restTemplate.postForEntity(
                url,
                entity,
                ConsentResponse.class);

        return response.getBody();
    }

    /**
     * Consulta status do consentimento
     */
    public ConsentResponse getConsent(String consentId) {
        String url = authorizationServer + "/open-banking/consents/v2/consents/" + consentId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-fapi-interaction-id", UUID.randomUUID().toString());

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<ConsentResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                ConsentResponse.class);

        return response.getBody();
    }
}