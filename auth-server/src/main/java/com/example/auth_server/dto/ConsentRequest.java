package com.example.auth_server.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConsentRequest {

    @JsonProperty("data")
    private Data data;

    private String clientId;

    public ConsentRequest() {
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
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

        public Data() {
        }

        public LoggedUser getLoggedUser() {
            return loggedUser;
        }

        public void setLoggedUser(LoggedUser loggedUser) {
            this.loggedUser = loggedUser;
        }

        public BusinessEntity getBusinessEntity() {
            return businessEntity;
        }

        public void setBusinessEntity(BusinessEntity businessEntity) {
            this.businessEntity = businessEntity;
        }

        public List<String> getPermissions() {
            return permissions;
        }

        public void setPermissions(List<String> permissions) {
            this.permissions = permissions;
        }

        public LocalDateTime getExpirationDateTime() {
            return expirationDateTime;
        }

        public void setExpirationDateTime(LocalDateTime expirationDateTime) {
            this.expirationDateTime = expirationDateTime;
        }

        public LocalDateTime getTransactionFromDateTime() {
            return transactionFromDateTime;
        }

        public void setTransactionFromDateTime(LocalDateTime transactionFromDateTime) {
            this.transactionFromDateTime = transactionFromDateTime;
        }

        public LocalDateTime getTransactionToDateTime() {
            return transactionToDateTime;
        }

        public void setTransactionToDateTime(LocalDateTime transactionToDateTime) {
            this.transactionToDateTime = transactionToDateTime;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LoggedUser {

        @JsonProperty("document")
        private Document document;

        public LoggedUser() {
        }

        public Document getDocument() {
            return document;
        }

        public void setDocument(Document document) {
            this.document = document;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BusinessEntity {

        @JsonProperty("document")
        private Document document;

        public BusinessEntity() {
        }

        public Document getDocument() {
            return document;
        }

        public void setDocument(Document document) {
            this.document = document;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Document {

        @JsonProperty("identification")
        private String identification;

        @JsonProperty("rel")
        private String rel;

        public Document() {
        }

        public String getIdentification() {
            return identification;
        }

        public void setIdentification(String identification) {
            this.identification = identification;
        }

        public String getRel() {
            return rel;
        }

        public void setRel(String rel) {
            this.rel = rel;
        }
    }
}