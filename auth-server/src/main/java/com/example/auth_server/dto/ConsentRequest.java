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

        @JsonProperty("loggedUser")
        @NotNull(message = "loggedUser é obrigatório")
        @Valid
        private LoggedUser loggedUser;

        @JsonProperty("businessEntity")
        @NotNull(message = "businessEntity é obrigatório")
        @Valid
        private BusinessEntity businessEntity;

        @JsonProperty("permissions")
        @NotEmpty(message = "permissions não pode estar vazio")
        private List<String> permissions;

        @JsonProperty("expirationDateTime")
        @NotNull(message = "expirationDateTime é obrigatório")
        private LocalDateTime expirationDateTime;

        @JsonProperty("transactionFromDateTime")
        @NotNull(message = "transactionFromDateTime é obrigatório")
        private LocalDateTime transactionFromDateTime;

        @JsonProperty("transactionToDateTime")
        @NotNull(message = "transactionToDateTime é obrigatório")
        private LocalDateTime transactionToDateTime;
    }

    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoggedUser {

        @JsonProperty("document")
        @NotNull(message = "loggedUser.document é obrigatório")
        @Valid
        private Document document;
    }

    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusinessEntity {

        @JsonProperty("document")
        @NotNull(message = "businessEntity.document é obrigatório")
        @Valid
        private Document document;
    }

    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Document {

        @JsonProperty("identification")
        @NotNull(message = "document.identification é obrigatório")
        private String identification;

        @JsonProperty("rel")
        @NotNull(message = "document.rel é obrigatório")
        private String rel;
    }
}