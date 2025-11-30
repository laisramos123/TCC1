package com.example.auth_server.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConsentResponse {

    @JsonProperty("data")
    private Data data;

    @JsonProperty("links")
    private Links links;

    @JsonProperty("meta")
    private Meta meta;

    public ConsentResponse() {
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    // ========== Data ==========
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

        public Data() {
        }

        public String getConsentId() {
            return consentId;
        }

        public void setConsentId(String consentId) {
            this.consentId = consentId;
        }

        public LocalDateTime getCreationDateTime() {
            return creationDateTime;
        }

        public void setCreationDateTime(LocalDateTime creationDateTime) {
            this.creationDateTime = creationDateTime;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public LocalDateTime getStatusUpdateDateTime() {
            return statusUpdateDateTime;
        }

        public void setStatusUpdateDateTime(LocalDateTime statusUpdateDateTime) {
            this.statusUpdateDateTime = statusUpdateDateTime;
        }

        public LocalDateTime getExpirationDateTime() {
            return expirationDateTime;
        }

        public void setExpirationDateTime(LocalDateTime expirationDateTime) {
            this.expirationDateTime = expirationDateTime;
        }

        public List<String> getPermissions() {
            return permissions;
        }

        public void setPermissions(List<String> permissions) {
            this.permissions = permissions;
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

    // ========== LoggedUser ==========
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

    // ========== BusinessEntity ==========
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

    // ========== Document ==========
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

    // ========== Links ==========
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

        public Links() {
        }

        public String getSelf() {
            return self;
        }

        public void setSelf(String self) {
            this.self = self;
        }

        public String getFirst() {
            return first;
        }

        public void setFirst(String first) {
            this.first = first;
        }

        public String getLast() {
            return last;
        }

        public void setLast(String last) {
            this.last = last;
        }

        public String getNext() {
            return next;
        }

        public void setNext(String next) {
            this.next = next;
        }

        public String getPrev() {
            return prev;
        }

        public void setPrev(String prev) {
            this.prev = prev;
        }
    }

    // ========== Meta ==========
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Meta {

        @JsonProperty("totalRecords")
        private Integer totalRecords;

        @JsonProperty("totalPages")
        private Integer totalPages;

        @JsonProperty("requestDateTime")
        private LocalDateTime requestDateTime;

        public Meta() {
        }

        public Integer getTotalRecords() {
            return totalRecords;
        }

        public void setTotalRecords(Integer totalRecords) {
            this.totalRecords = totalRecords;
        }

        public Integer getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(Integer totalPages) {
            this.totalPages = totalPages;
        }

        public LocalDateTime getRequestDateTime() {
            return requestDateTime;
        }

        public void setRequestDateTime(LocalDateTime requestDateTime) {
            this.requestDateTime = requestDateTime;
        }
    }
}