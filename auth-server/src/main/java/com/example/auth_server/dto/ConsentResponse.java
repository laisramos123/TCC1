package com.example.auth_server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConsentResponse {

    @JsonProperty("data")
    private Data data;

    @JsonProperty("links")
    private Links links;

    @JsonProperty("meta")
    private Meta meta;

    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Data {

        /**
         * Identificador único do consentimento
         * Formato: URN (Uniform Resource Name)
         * Ex: "urn:banco:C1DD93123"
         */
        @JsonProperty("consentId")
        private String consentId;

        /**
         * Data e hora de criação do consentimento
         * Formato: ISO 8601 (yyyy-MM-dd'T'HH:mm:ss'Z')
         */
        @JsonProperty("creationDateTime")
        private LocalDateTime creationDateTime;

        @JsonProperty("status")
        private String status;

        /**
         * Data e hora da última atualização de status
         * Formato: ISO 8601
         */
        @JsonProperty("statusUpdateDateTime")
        private LocalDateTime statusUpdateDateTime;

        /**
         * Data e hora de expiração do consentimento
         * Máximo: 60 dias a partir da criação
         * Formato: ISO 8601
         */
        @JsonProperty("expirationDateTime")
        private LocalDateTime expirationDateTime;

        /**
         * Lista de permissões concedidas
         */
        @JsonProperty("permissions")
        private List<String> permissions;

        /**
         * Usuário logado que autorizou o consentimento
         */
        @JsonProperty("loggedUser")
        private LoggedUser loggedUser;

        /**
         * Entidade de negócio relacionada
         */
        @JsonProperty("businessEntity")
        private BusinessEntity businessEntity;

        /**
         * Data e hora inicial para consulta de transações
         */
        @JsonProperty("transactionFromDateTime")
        private LocalDateTime transactionFromDateTime;

        /**
         * Data e hora final para consulta de transações
         */
        @JsonProperty("transactionToDateTime")
        private LocalDateTime transactionToDateTime;

        /**
         * Razão de rejeição (quando status = REJECTED)
         * Presente apenas se o consentimento foi rejeitado
         */
        @JsonProperty("rejectionReason")
        private RejectionReason rejectionReason;

        /**
         * Razão de revogação (quando status = REVOKED)
         * Presente apenas se o consentimento foi revogado
         */
        @JsonProperty("revocationReason")
        private RevocationReason revocationReason;
    }

    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LoggedUser {

        @JsonProperty("document")
        private Document document;
    }

    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BusinessEntity {

        @JsonProperty("document")
        private Document document;
    }

    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Document {

        /**
         * Número do documento (CPF ou CNPJ)
         */
        @JsonProperty("identification")
        private String identification;

        /**
         * Tipo de relacionamento: CPF ou CNPJ
         */
        @JsonProperty("rel")
        private String rel;
    }

    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RejectionReason {

        @JsonProperty("code")
        private String code;

        /**
         * Descrição detalhada da rejeição
         */
        @JsonProperty("detail")
        private String detail;

        /**
         * Informações adicionais sobre a rejeição
         */
        @JsonProperty("additionalInformation")
        private String additionalInformation;
    }

    /**
     * Razão de revogação do consentimento
     */
    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RevocationReason {

        @JsonProperty("code")
        private String code;

        /**
         * Descrição detalhada da revogação
         */
        @JsonProperty("detail")
        private String detail;

        /**
         * Informações adicionais sobre a revogação
         */
        @JsonProperty("additionalInformation")
        private String additionalInformation;

        /**
         * Quem revogou o consentimento
         * 
         * Valores possíveis:
         * - USER: Revogado pelo usuário
         * - TPP: Revogado pela instituição iniciadora
         * - ASPSP: Revogado pela instituição detentora
         */
        @JsonProperty("revokedBy")
        private String revokedBy;

        /**
         * Data e hora da revogação
         * Formato: ISO 8601
         */
        @JsonProperty("revokedAt")
        private LocalDateTime revokedAt;
    }

    /**
     * Links HATEOAS para navegação
     */
    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Links {

        /**
         * URI completo do recurso atual
         * Ex:
         * "https://api.banco.com.br/open-banking/consents/v2/consents/urn:banco:C1DD93123"
         */
        @JsonProperty("self")
        private String self;

        /**
         * Link para primeira página (em caso de paginação)
         */
        @JsonProperty("first")
        private String first;

        /**
         * Link para última página (em caso de paginação)
         */
        @JsonProperty("last")
        private String last;

        /**
         * Link para próxima página (em caso de paginação)
         */
        @JsonProperty("next")
        private String next;

        /**
         * Link para página anterior (em caso de paginação)
         */
        @JsonProperty("prev")
        private String prev;
    }

    /**
     * Metadados da resposta
     */
    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Meta {

        /**
         * Total de registros retornados
         */
        @JsonProperty("totalRecords")
        private Integer totalRecords;

        /**
         * Total de páginas disponíveis
         */
        @JsonProperty("totalPages")
        private Integer totalPages;

        /**
         * Data e hora da requisição
         * Formato: ISO 8601
         */
        @JsonProperty("requestDateTime")
        private LocalDateTime requestDateTime;
    }
}