package com.example.auth_server.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ConsentResponse {
    private Data data;
    private Links links;
    private Meta meta;
    
    public ConsentResponse() {}
    
    // Classes internas
    public static class Data {
        private String consentId;
        private LocalDateTime creationDateTime;
        public Data() {}
        public String getConsentId() { return consentId; }
        public void setConsentId(String consentId) { this.consentId = consentId; }
    }
    
    public static class Links {
        private String self;
        public Links() {}
    }
    
    public static class Meta {
        private String totalRecords;
        public Meta() {}
    }
    
    public static class LoggedUser {
        public LoggedUser() {}
    }
    
    public static class BusinessEntity {
        public BusinessEntity() {}
    }
    
    public static class Document {
        public Document() {}
    }
    
    // Getters e Setters
    public Data getData() { return data; }
    public void setData(Data data) { this.data = data; }
}
