package com.example.auth_client.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.auth_client.model.Transaction;

@Service
public class TransactionService {

    @Value("${resource-server.api-base-url}")
    private String resourceServerUrl;

    private final WebClient webClient;

    public TransactionService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public List<Transaction> getTransactions(OAuth2AuthorizedClient authorizedClient) {
        String accessToken = authorizedClient.getAccessToken().getTokenValue();

        return webClient
                .get()
                .uri(resourceServerUrl + "/api/transactions")
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(Transaction[].class)
                .map(Arrays::asList)
                .block(); // Bloqueante para simplificar o exemplo
    }

    public List<Transaction> getTransactionsByDate(OAuth2AuthorizedClient authorizedClient,
            LocalDate startDate,
            LocalDate endDate) {
        String accessToken = authorizedClient.getAccessToken().getTokenValue();

        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(resourceServerUrl + "/api/transactions/filter")
                        .queryParam("startDate", startDate)
                        .queryParam("endDate", endDate)
                        .build())
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(Transaction[].class)
                .map(Arrays::asList)
                .block(); // Bloqueante para simplificar o exemplo
    }
}
