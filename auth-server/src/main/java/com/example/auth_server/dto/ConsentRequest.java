package com.example.auth_server.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ConsentRequest {
    private Data data;
    
    public static class Data {
        private LoggedUser loggedUser;
        private BusinessEntity businessEntity;
        private List<String> permissions;
        private LocalDateTime expirationDateTime;
        
        public static class LoggedUser {
            private Document document;
            
            public static class Document {
                private String identification;
                private String rel;
                
                public String getIdentification() { return identification; }
                public void setIdentification(String identification) { this.identification = identification; }
                public String getRel() { return rel; }
                public void setRel(String rel) { this.rel = rel; }
            }
            
            public Document getDocument() { return document; }
            public void setDocument(Document document) { this.document = document; }
        }
        
        public static class BusinessEntity {
            private Document document;
            
            public static class Document {
                private String identification;
                private String rel;
                
                public String getIdentification() { return identification; }
                public void setIdentification(String identification) { this.identification = identification; }
                public String getRel() { return rel; }
                public void setRel(String rel) { this.rel = rel; }
            }
            
            public Document getDocument() { return document; }
            public void setDocument(Document document) { this.document = document; }
        }
        
        public LoggedUser getLoggedUser() { return loggedUser; }
        public void setLoggedUser(LoggedUser loggedUser) { this.loggedUser = loggedUser; }
        public BusinessEntity getBusinessEntity() { return businessEntity; }
        public void setBusinessEntity(BusinessEntity businessEntity) { this.businessEntity = businessEntity; }
        public List<String> getPermissions() { return permissions; }
        public void setPermissions(List<String> permissions) { this.permissions = permissions; }
        public LocalDateTime getExpirationDateTime() { return expirationDateTime; }
        public void setExpirationDateTime(LocalDateTime expirationDateTime) { this.expirationDateTime = expirationDateTime; }
    }
    
    private String clientId;
    
    public Data getData() { return data; }
    public void setData(Data data) { this.data = data; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
}
