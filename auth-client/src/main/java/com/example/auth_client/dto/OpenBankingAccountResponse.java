package com.example.auth_client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenBankingAccountResponse {

    @JsonProperty("data")
    private List<AccountData> data;

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
}
