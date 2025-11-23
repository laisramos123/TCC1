package com.example.auth_server.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import com.example.auth_server.enums.ConsentStatus;

@Entity
@Table(name = "consents")
public class Consent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String consentId;
    
    private String clientId;
    private String userId;
    private ConsentStatus status;
    private LocalDateTime creationDateTime;
    private LocalDateTime statusUpdateDateTime;
    private LocalDateTime expirationDateTime;
    
    @ElementCollection
    private List<String> permissions;
    
    private String loggedUserDocument;
    private String loggedUserRel;
    private String businessEntityDocument;
    private String businessEntityRel;
    
    private LocalDateTime transactionFromDateTime;
    private LocalDateTime transactionToDateTime;
    
    private String revocationReasonCode;
    private String revocationReasonDetail;
    private String revokedBy;
    private LocalDateTime revokedAt;
    
    // Constructors
    public Consent() {}
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Builder class
    public static class Builder {
        private Consent consent = new Consent();
        
        public Builder consentId(String consentId) {
            consent.consentId = consentId;
            return this;
        }
        
        public Builder clientId(String clientId) {
            consent.clientId = clientId;
            return this;
        }
        
        public Builder userId(String userId) {
            consent.userId = userId;
            return this;
        }
        
        public Builder status(ConsentStatus status) {
            consent.status = status;
            return this;
        }
        
        public Builder creationDateTime(LocalDateTime creationDateTime) {
            consent.creationDateTime = creationDateTime;
            return this;
        }
        
        public Builder statusUpdateDateTime(LocalDateTime statusUpdateDateTime) {
            consent.statusUpdateDateTime = statusUpdateDateTime;
            return this;
        }
        
        public Builder expirationDateTime(LocalDateTime expirationDateTime) {
            consent.expirationDateTime = expirationDateTime;
            return this;
        }
        
        public Builder permissions(List<String> permissions) {
            consent.permissions = permissions;
            return this;
        }
        
        public Builder loggedUserDocument(String loggedUserDocument) {
            consent.loggedUserDocument = loggedUserDocument;
            return this;
        }
        
        public Builder loggedUserRel(String loggedUserRel) {
            consent.loggedUserRel = loggedUserRel;
            return this;
        }
        
        public Builder businessEntityDocument(String businessEntityDocument) {
            consent.businessEntityDocument = businessEntityDocument;
            return this;
        }
        
        public Builder businessEntityRel(String businessEntityRel) {
            consent.businessEntityRel = businessEntityRel;
            return this;
        }
        
        public Builder transactionFromDateTime(LocalDateTime transactionFromDateTime) {
            consent.transactionFromDateTime = transactionFromDateTime;
            return this;
        }
        
        public Builder transactionToDateTime(LocalDateTime transactionToDateTime) {
            consent.transactionToDateTime = transactionToDateTime;
            return this;
        }
        
        public Consent build() {
            return consent;
        }
    }
    
    // Getters and Setters
    public String getConsentId() { return consentId; }
    public void setConsentId(String consentId) { this.consentId = consentId; }
    
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public ConsentStatus getStatus() { return status; }
    public void setStatus(ConsentStatus status) { this.status = status; }
    
    public LocalDateTime getCreationDateTime() { return creationDateTime; }
    public void setCreationDateTime(LocalDateTime creationDateTime) { this.creationDateTime = creationDateTime; }
    
    public LocalDateTime getStatusUpdateDateTime() { return statusUpdateDateTime; }
    public void setStatusUpdateDateTime(LocalDateTime statusUpdateDateTime) { this.statusUpdateDateTime = statusUpdateDateTime; }
    
    public LocalDateTime getExpirationDateTime() { return expirationDateTime; }
    public void setExpirationDateTime(LocalDateTime expirationDateTime) { this.expirationDateTime = expirationDateTime; }
    
    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
    
    public String getLoggedUserDocument() { return loggedUserDocument; }
    public void setLoggedUserDocument(String loggedUserDocument) { this.loggedUserDocument = loggedUserDocument; }
    
    public String getLoggedUserRel() { return loggedUserRel; }
    public void setLoggedUserRel(String loggedUserRel) { this.loggedUserRel = loggedUserRel; }
    
    public String getBusinessEntityDocument() { return businessEntityDocument; }
    public void setBusinessEntityDocument(String businessEntityDocument) { this.businessEntityDocument = businessEntityDocument; }
    
    public String getBusinessEntityRel() { return businessEntityRel; }
    public void setBusinessEntityRel(String businessEntityRel) { this.businessEntityRel = businessEntityRel; }
    
    public LocalDateTime getTransactionFromDateTime() { return transactionFromDateTime; }
    public void setTransactionFromDateTime(LocalDateTime transactionFromDateTime) { this.transactionFromDateTime = transactionFromDateTime; }
    
    public LocalDateTime getTransactionToDateTime() { return transactionToDateTime; }
    public void setTransactionToDateTime(LocalDateTime transactionToDateTime) { this.transactionToDateTime = transactionToDateTime; }
    
    public String getRevocationReasonCode() { return revocationReasonCode; }
    public void setRevocationReasonCode(String revocationReasonCode) { this.revocationReasonCode = revocationReasonCode; }
    
    public String getRevocationReasonDetail() { return revocationReasonDetail; }
    public void setRevocationReasonDetail(String revocationReasonDetail) { this.revocationReasonDetail = revocationReasonDetail; }
    
    public String getRevokedBy() { return revokedBy; }
    public void setRevokedBy(String revokedBy) { this.revokedBy = revokedBy; }
    
    public LocalDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }
}
