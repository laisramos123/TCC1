package com.example.resource_server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

    @JsonProperty("data")
    private List<AccountData> data;

    @JsonProperty("links")
    private Links links;

    @JsonProperty("meta")
    private Meta meta;

    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountData {

        @JsonProperty("accountId")
        private String accountId;

        @JsonProperty("accountNumber")
        private String accountNumber;

        @JsonProperty("accountType")
        private String accountType;

        @JsonProperty("balance")
        private BigDecimal balance;

        @JsonProperty("currency")
        private String currency;

        @JsonProperty("status")
        private String status;
    }

    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Links {

        @JsonProperty("self")
        private String self;
    }

    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta {

        @JsonProperty("totalRecords")
        private Integer totalRecords;

        @JsonProperty("totalPages")
        private Integer totalPages;

        @JsonProperty("requestDateTime")
        private LocalDateTime requestDateTime;
    }
}