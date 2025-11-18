package com.example.resource_server.dto;

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
public class ConsentResponse {

    @JsonProperty("data")
    private Data data;

    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {

        @JsonProperty("consentId")
        private String consentId;

        @JsonProperty("status")
        private String status;

        @JsonProperty("expirationDateTime")
        private LocalDateTime expirationDateTime;

        @JsonProperty("permissions")
        private List<String> permissions;
    }
}