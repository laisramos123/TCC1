package com.example.auth_client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Request para criar consentimento no Authorization Server
 * POST /open-banking/consents/v2/consents
 * 
 * Client TPP → Authorization Server
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentRequest {

    @JsonProperty("data")
    private Data data;

    /**
     * Dados do consentimento
     */
    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {

        /**
         * Usuário logado (titular dos dados)
         */
        @JsonProperty("loggedUser")
        private LoggedUser loggedUser;

        /**
         * Entidade de negócio relacionada ao consentimento
         */
        @JsonProperty("businessEntity")
        private BusinessEntity businessEntity;

        /**
         * Lista de permissões solicitadas
         * 
         * Exemplos:
         * - ACCOUNTS_READ
         * - ACCOUNTS_BALANCES_READ
         * - ACCOUNTS_TRANSACTIONS_READ
         * - CREDIT_CARDS_ACCOUNTS_READ
         * - CUSTOMERS_PERSONAL_IDENTIFICATIONS_READ
         * - RESOURCES_READ
         */
        @JsonProperty("permissions")
        private List<String> permissions;

        /**
         * Data/hora de expiração do consentimento
         * Máximo: 60 dias a partir da criação
         */
        @JsonProperty("expirationDateTime")
        private LocalDateTime expirationDateTime;

        /**
         * Data/hora inicial para consulta de transações
         */
        @JsonProperty("transactionFromDateTime")
        private LocalDateTime transactionFromDateTime;

        /**
         * Data/hora final para consulta de transações
         */
        @JsonProperty("transactionToDateTime")
        private LocalDateTime transactionToDateTime;
    }

    /**
     * Usuário logado
     */
    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoggedUser {

        @JsonProperty("document")
        private Document document;
    }

    /**
     * Entidade de negócio
     */
    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusinessEntity {

        @JsonProperty("document")
        private Document document;
    }

    /**
     * Documento de identificação (CPF ou CNPJ)
     */
    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Document {

        /**
         * Número do documento
         * CPF: 11 dígitos (ex: "12345678900")
         * CNPJ: 14 dígitos (ex: "12345678000195")
         */
        @JsonProperty("identification")
        private String identification;

        /**
         * Tipo de relacionamento: "CPF" ou "CNPJ"
         */
        @JsonProperty("rel")
        private String rel;
    }
}