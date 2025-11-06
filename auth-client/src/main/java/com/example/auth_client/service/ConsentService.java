package com.example.auth_client.service;

import com.example.auth_client.dto.ConsentRequest;
import com.example.auth_client.dto.ConsentResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ConsentService {

        @Value("${tpp.bank.authorization-server}")
        private String authorizationServer;

        private final RestTemplate restTemplate; // Agora usa mTLS automaticamente

        public ConsentService(@Qualifier("mtlsRestTemplate") RestTemplate restTemplate) {
                this.restTemplate = restTemplate;
        }

        /**
         * PASSO 1: Cria consentimento no banco (COM mTLS)
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
                                                .expirationDateTime(LocalDateTime.now().plusDays(60))
                                                .transactionFromDateTime(LocalDateTime.now().minusYears(1))
                                                .transactionToDateTime(LocalDateTime.now())
                                                .build())
                                .build();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("x-fapi-interaction-id", UUID.randomUUID().toString());

                HttpEntity<ConsentRequest> entity = new HttpEntity<>(request, headers);

                // RestTemplate usa mTLS automaticamente
                ResponseEntity<ConsentResponse> response = restTemplate.postForEntity(
                                url,
                                entity,
                                ConsentResponse.class);

                return response.getBody();
        }

        /**
         * Consulta status do consentimento (COM mTLS)
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