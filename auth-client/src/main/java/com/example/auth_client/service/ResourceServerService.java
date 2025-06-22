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

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ResourceServerService {

    private final WebClient webClient;

    @Value("${resource-server.api-base-url}")
    private String apiBaseUrl;

    public ResourceServerService(WebClient webClient) {
        this.webClient = webClient;
    }

    public List<Account> getAccounts(OAuth2AuthorizedClient authorizedClient) {
        try {
            log.info("  Buscando contas no Resource Server: {}", apiBaseUrl);

            String accessToken = authorizedClient.getAccessToken().getTokenValue();
            log.debug("  Token de acesso: {}...", accessToken.substring(0, Math.min(20, accessToken.length())));

            List<Account> accounts = webClient
                    .get()
                    .uri(apiBaseUrl + "/accounts")
                    .headers(headers -> headers.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Account>>() {
                    })
                    .block();

            log.info(" {} contas encontradas", accounts != null ? accounts.size() : 0);
            return accounts;

        } catch (WebClientResponseException e) {
            log.error("  Erro HTTP ao buscar contas: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Erro ao buscar contas: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("  Erro geral ao buscar contas", e);
            throw new RuntimeException("Erro ao buscar contas: " + e.getMessage(), e);
        }
    }

    public Account getAccountById(OAuth2AuthorizedClient authorizedClient, Long id) {
        try {
            log.info("  Buscando conta ID {} no Resource Server", id);

            return webClient
                    .get()
                    .uri(apiBaseUrl + "/accounts/{id}", id)
                    .headers(headers -> headers.setBearerAuth(authorizedClient.getAccessToken().getTokenValue()))
                    .retrieve()
                    .bodyToMono(Account.class)
                    .block();

        } catch (WebClientResponseException e) {
            log.error("  Erro HTTP ao buscar conta {}: {} - {}", id, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Erro ao buscar conta com ID " + id + ": " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("  Erro geral ao buscar conta {}", id, e);
            throw new RuntimeException("Erro ao buscar conta com ID " + id + ": " + e.getMessage(), e);
        }
    }

    public List<Transaction> getTransactions(OAuth2AuthorizedClient authorizedClient) {
        try {
            log.info("  Buscando transacões no Resource Server");

            List<Transaction> transactions = webClient
                    .get()
                    .uri(apiBaseUrl + "/transactions")
                    .headers(headers -> headers.setBearerAuth(authorizedClient.getAccessToken().getTokenValue()))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Transaction>>() {
                    })
                    .block();

            log.info("  {} transacões encontradas", transactions != null ? transactions.size() : 0);
            return transactions;

        } catch (WebClientResponseException e) {
            log.error("  Erro HTTP ao buscar transacões: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Erro ao buscar transacões: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("  Erro geral ao buscar transacões", e);
            throw new RuntimeException("Erro ao buscar transacões: " + e.getMessage(), e);
        }
    }

    public List<Transaction> getTransactionsByDate(OAuth2AuthorizedClient authorizedClient,
            LocalDate startDate, LocalDate endDate) {
        try {
            log.info("  Buscando transacões por período: {} a {}", startDate, endDate);

            List<Transaction> transactions = webClient
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

            log.info(" {} transacões encontradas no período", transactions != null ? transactions.size() : 0);
            return transactions;

        } catch (WebClientResponseException e) {
            log.error("  Erro HTTP ao buscar transacões por data: {} - {}", e.getStatusCode(),
                    e.getResponseBodyAsString());
            throw new RuntimeException("Erro ao buscar transacões por data: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("  Erro geral ao buscar transacões por data", e);
            throw new RuntimeException("Erro ao buscar transacões por data: " + e.getMessage(), e);
        }
    }
}