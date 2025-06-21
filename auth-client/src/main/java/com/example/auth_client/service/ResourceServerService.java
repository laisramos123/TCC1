package com.example.auth_client.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.example.auth_client.model.Account;
import com.example.auth_client.model.Transaction;

@Service
public class ResourceServerService {

    private final WebClient webClient;

    @Value("${resource-server.api-base-url}")
    private String apiBaseUrl;

    public ResourceServerService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public List<Account> getAccounts(OAuth2AuthorizedClient authorizedClient) {
        try {
            return webClient
                    .get()
                    .uri(apiBaseUrl + "/accounts")
                    .headers(headers -> headers.setBearerAuth(authorizedClient.getAccessToken().getTokenValue()))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Account>>() {
                    })
                    .block();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Erro ao buscar contas: " + e.getMessage(), e);
        }
    }

    public Account getAccountById(OAuth2AuthorizedClient authorizedClient, Long id) {
        try {
            return webClient
                    .get()
                    .uri(apiBaseUrl + "/accounts/{id}", id)
                    .headers(headers -> headers.setBearerAuth(authorizedClient.getAccessToken().getTokenValue()))
                    .retrieve()
                    .bodyToMono(Account.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Erro ao buscar conta com ID " + id + ": " + e.getMessage(), e);
        }
    }

    public List<Transaction> getTransactions(OAuth2AuthorizedClient authorizedClient) {
        try {
            return webClient
                    .get()
                    .uri(apiBaseUrl + "/transactions")
                    .headers(headers -> headers.setBearerAuth(authorizedClient.getAccessToken().getTokenValue()))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Transaction>>() {
                    })
                    .block();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Erro ao buscar transações: " + e.getMessage(), e);
        }
    }

    public List<Transaction> getTransactionsByDate(OAuth2AuthorizedClient authorizedClient,
            LocalDate startDate,
            LocalDate endDate) {
        try {
            return webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path(apiBaseUrl + "/transactions/filter")
                            .queryParam("startDate", startDate.toString())
                            .queryParam("endDate", endDate.toString())
                            .build())
                    .headers(headers -> headers.setBearerAuth(authorizedClient.getAccessToken().getTokenValue()))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Transaction>>() {
                    })
                    .block();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Erro ao buscar transações por data: " + e.getMessage(), e);
        }
    }
}