package com.example.auth_server.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ConsentRequest {
    private Data data;
    
    public ConsentRequest() {}
    
    public static class Data {
        private String loggedUser;
        private List<String> permissions;
        private LocalDateTime expirationDateTime;
        
        public Data() {}
        
        public String getLoggedUser() { return loggedUser; }
        public void setLoggedUser(String loggedUser) { this.loggedUser = loggedUser; }
        
        public List<String> getPermissions() { return permissions; }
        public void setPermissions(List<String> permissions) { this.permissions = permissions; }
        
        public LocalDateTime getExpirationDateTime() { return expirationDateTime; }
        public void setExpirationDateTime(LocalDateTime expirationDateTime) { this.expirationDateTime = expirationDateTime; }
    }
    
    public Data getData() { return data; }
    public void setData(Data data) { this.data = data; }
}
