package com.example.auth_client.dto;

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

        @JsonProperty("consentId")
        private String consentId;

        @JsonProperty("creationDateTime")
        private LocalDateTime creationDateTime;

        @JsonProperty("status")
        private String status;

        @JsonProperty("statusUpdateDateTime")
        private LocalDateTime statusUpdateDateTime;

        @JsonProperty("expirationDateTime")
        private LocalDateTime expirationDateTime;

        @JsonProperty("permissions")
        private List<String> permissions;

        @JsonProperty("loggedUser")
        private LoggedUser loggedUser;

        @JsonProperty("businessEntity")
        private BusinessEntity businessEntity;

        @JsonProperty("transactionFromDateTime")
        private LocalDateTime transactionFromDateTime;

        @JsonProperty("transactionToDateTime")
        private LocalDateTime transactionToDateTime;

        @JsonProperty("rejectionReason")
        private RejectionReason rejectionReason;

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

        @JsonProperty("identification")
        private String identification;

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

        @JsonProperty("detail")
        private String detail;

        @JsonProperty("additionalInformation")
        private String additionalInformation;
    }

    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RevocationReason {

        @JsonProperty("code")
        private String code;

        @JsonProperty("detail")
        private String detail;

        @JsonProperty("additionalInformation")
        private String additionalInformation;

        @JsonProperty("revokedBy")
        private String revokedBy;

        @JsonProperty("revokedAt")
        private LocalDateTime revokedAt;
    }

    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Links {

        @JsonProperty("self")
        private String self;

        @JsonProperty("first")
        private String first;

        @JsonProperty("last")
        private String last;

        @JsonProperty("next")
        private String next;

        @JsonProperty("prev")
        private String prev;
    }

    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Meta {

        @JsonProperty("totalRecords")
        private Integer totalRecords;

        @JsonProperty("totalPages")
        private Integer totalPages;

        @JsonProperty("requestDateTime")
        private LocalDateTime requestDateTime;
    }
}
