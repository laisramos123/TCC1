package com.example.auth_server.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ConsentResponse {
    private Data data;
    private Links links;
    private Meta meta;

    public static class Data {
        private String consentId;
        private String status;
        private LocalDateTime creationDateTime;
        private LocalDateTime statusUpdateDateTime;
        private LocalDateTime expirationDateTime;
        private List<String> permissions;
        private LoggedUser loggedUser;
        private BusinessEntity businessEntity;

        public String getConsentId() {
            return consentId;
        }

        public void setConsentId(String consentId) {
            this.consentId = consentId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public LocalDateTime getCreationDateTime() {
            return creationDateTime;
        }

        public void setCreationDateTime(LocalDateTime creationDateTime) {
            this.creationDateTime = creationDateTime;
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
    }

    public static class LoggedUser {
        private Document document;

        public Document getDocument() {
            return document;
        }

        public void setDocument(Document document) {
            this.document = document;
        }
    }

    public static class BusinessEntity {
        private Document document;

        public Document getDocument() {
            return document;
        }

        public void setDocument(Document document) {
            this.document = document;
        }
    }

    public static class Document {
        private String identification;
        private String rel;

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

    public static class Links {
        private String self;

        public String getSelf() {
            return self;
        }

        public void setSelf(String self) {
            this.self = self;
        }
    }

    public static class Meta {
        private int totalRecords;
        private int totalPages;
        private LocalDateTime requestDateTime;

        public int getTotalRecords() {
            return totalRecords;
        }

        public void setTotalRecords(int totalRecords) {
            this.totalRecords = totalRecords;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }

        public LocalDateTime getRequestDateTime() {
            return requestDateTime;
        }

        public void setRequestDateTime(LocalDateTime requestDateTime) {
            this.requestDateTime = requestDateTime;
        }

        public static Object builder() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'builder'");
        }
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
}
