package com.example.auth_server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentRequest {

    @JsonProperty("data")
    @NotNull(message = "data é obrigatório")
    @Valid
    private Data data;

    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {

        /**
         * Usuário logado que está autorizando o consentimento
         * Representa quem está dando permissão
         */
        @JsonProperty("loggedUser")
        @NotNull(message = "loggedUser é obrigatório")
        @Valid
        private LoggedUser loggedUser;

        /**
         * Entidade de negócio relacionada ao consentimento
         * Pode ser a mesma pessoa do loggedUser ou diferente
         */
        @JsonProperty("businessEntity")
        @NotNull(message = "businessEntity é obrigatório")
        @Valid
        private BusinessEntity businessEntity;

        /**
         * Lista de permissões solicitadas
         */
        @JsonProperty("permissions")
        @NotEmpty(message = "permissions não pode estar vazio")
        private List<String> permissions;

        /**
         * Data e hora de expiração do consentimento
         */
        @JsonProperty("expirationDateTime")
        @NotNull(message = "expirationDateTime é obrigatório")
        private LocalDateTime expirationDateTime;

        /**
         * Data e hora inicial para consulta de transações
         */
        @JsonProperty("transactionFromDateTime")
        @NotNull(message = "transactionFromDateTime é obrigatório")
        private LocalDateTime transactionFromDateTime;

        /**
         * Data e hora final para consulta de transações
         */
        @JsonProperty("transactionToDateTime")
        @NotNull(message = "transactionToDateTime é obrigatório")
        private LocalDateTime transactionToDateTime;
    }

    /**
     * Usuário logado (titular)
     */
    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoggedUser {

        /**
         * Documento de identificação do usuário logado
         */
        @JsonProperty("document")
        @NotNull(message = "loggedUser.document é obrigatório")
        @Valid
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

        /**
         * Documento de identificação da entidade de negócio
         */
        @JsonProperty("document")
        @NotNull(message = "businessEntity.document é obrigatório")
        @Valid
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
         * 
         * Formatos aceitos:
         * - CPF: 11 dígitos (sem pontuação)
         * Ex: "12345678900"
         * 
         * - CNPJ: 14 dígitos (sem pontuação)
         * Ex: "12345678000100"
         */
        @JsonProperty("identification")
        @NotNull(message = "document.identification é obrigatório")
        private String identification;

        /**
         * Tipo de relacionamento do documento
         * 
         * Valores possíveis:
         * - "CPF": Pessoa Física
         * - "CNPJ": Pessoa Jurídica
         */
        @JsonProperty("rel")
        @NotNull(message = "document.rel é obrigatório")
        private String rel;
    }
}