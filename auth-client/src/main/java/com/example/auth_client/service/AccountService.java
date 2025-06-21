package com.example.auth_client.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.auth_client.model.Account;

@Service
public class AccountService {

    @Value("${resource-server.api-base-url}")
    private String resourceServerUrl;

    private final WebClient webClient;

    public AccountService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public List<Account> getAccounts(OAuth2AuthorizedClient authorizedClient) {
        String accessToken = authorizedClient.getAccessToken().getTokenValue();

        return webClient
                .get()
                .uri(resourceServerUrl + "/api/accounts")
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(Account[].class)
                .map(Arrays::asList)
                .block(); // Bloqueante para simplificar o exemplo
    }

    public Account getAccountById(OAuth2AuthorizedClient authorizedClient, Long accountId) {
        String accessToken = authorizedClient.getAccessToken().getTokenValue();

        return webClient
                .get()
                .uri(resourceServerUrl + "/api/accounts/" + accountId)
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(Account.class)
                .block(); // Bloqueante para simplificar o exemplo
    }
}
