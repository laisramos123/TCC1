package com.example.auth_client.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.auth_client.dto.OpenBankingAccountResponse;

import java.util.UUID;

@Service
public class AccountService {

    @Value("${tpp.bank.resource-server}")
    private String resourceServer;

    private final RestTemplate restTemplate;

    public AccountService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * PASSO 4: Busca contas usando access token
     */
    public OpenBankingAccountResponse getAccounts(String accessToken) {

        String url = resourceServer + "/open-banking/accounts/v2/accounts";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set("x-fapi-interaction-id", UUID.randomUUID().toString());

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<OpenBankingAccountResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                OpenBankingAccountResponse.class);

        return response.getBody();
    }

    /**
     * Busca saldo de uma conta
     */
    public Object getAccountBalance(String accountId, String accessToken) {

        String url = resourceServer + "/open-banking/accounts/v2/accounts/"
                + accountId + "/balances";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set("x-fapi-interaction-id", UUID.randomUUID().toString());

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Object> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Object.class);

        return response.getBody();
    }
}
