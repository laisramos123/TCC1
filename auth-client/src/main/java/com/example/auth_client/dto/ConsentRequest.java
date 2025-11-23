package com.example.auth_client.dto;

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
public class ConsentRequest {

    @JsonProperty("data")
    private Data data;

    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {

        @JsonProperty("loggedUser")
        private LoggedUser loggedUser;

        @JsonProperty("businessEntity")
        private BusinessEntity businessEntity;

        @JsonProperty("permissions")
        private List<String> permissions;

        @JsonProperty("expirationDateTime")
        private LocalDateTime expirationDateTime;

        @JsonProperty("transactionFromDateTime")
        private LocalDateTime transactionFromDateTime;

        @JsonProperty("transactionToDateTime")
        private LocalDateTime transactionToDateTime;
    }

    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoggedUser {

        @JsonProperty("document")
        private Document document;
    }

    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusinessEntity {

        @JsonProperty("document")
        private Document document;
    }

    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Document {

        @JsonProperty("identification")
        private String identification;

        @JsonProperty("rel")
        private String rel;
    }
}