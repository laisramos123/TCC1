package com.example.auth_server.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

import com.example.auth_server.enums.ConsentStatus;

@Entity
@Table(name = "consents")
public class Consent {
    @Id
    @Column(name = "consent_id")
    private String consentId;

    @Column(name = "client_id")
    private String clientId;

    @Column(name = "user_id")
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ConsentStatus status;

    @Column(name = "creation_date_time")
    private LocalDateTime creationDateTime;

    @Column(name = "status_update_date_time")
    private LocalDateTime statusUpdateDateTime;

    @Column(name = "expiration_date_time")
    private LocalDateTime expirationDateTime;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "consent_permissions", joinColumns = @JoinColumn(name = "consent_id"))
    @Column(name = "permission")
    private List<String> permissions;

    @Column(name = "logged_user_document")
    private String loggedUserDocument;

    @Column(name = "logged_user_rel")
    private String loggedUserRel;

    @Column(name = "business_entity_document")
    private String businessEntityDocument;

    @Column(name = "business_entity_rel")
    private String businessEntityRel;

    @Column(name = "transaction_from_date_time")
    private LocalDateTime transactionFromDateTime;

    @Column(name = "transaction_to_date_time")
    private LocalDateTime transactionToDateTime;

    @Column(name = "revocation_reason_code")
    private String revocationReasonCode;

    @Column(name = "revocation_reason_detail")
    private String revocationReasonDetail;

    @Column(name = "revoked_by")
    private String revokedBy;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    public Consent() {
    }

    public static Builder builder() {
        return new Builder();
    }

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

        public Consent build() {
            return consent;
        }
    }

    public String getConsentId() {
        return consentId;
    }

    public void setConsentId(String consentId) {
        this.consentId = consentId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ConsentStatus getStatus() {
        return status;
    }

    public void setStatus(ConsentStatus status) {
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

    public String getLoggedUserDocument() {
        return loggedUserDocument;
    }

    public void setLoggedUserDocument(String loggedUserDocument) {
        this.loggedUserDocument = loggedUserDocument;
    }

    public String getLoggedUserRel() {
        return loggedUserRel;
    }

    public void setLoggedUserRel(String loggedUserRel) {
        this.loggedUserRel = loggedUserRel;
    }

    public String getBusinessEntityDocument() {
        return businessEntityDocument;
    }

    public void setBusinessEntityDocument(String businessEntityDocument) {
        this.businessEntityDocument = businessEntityDocument;
    }

    public String getBusinessEntityRel() {
        return businessEntityRel;
    }

    public void setBusinessEntityRel(String businessEntityRel) {
        this.businessEntityRel = businessEntityRel;
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

    public String getRevocationReasonCode() {
        return revocationReasonCode;
    }

    public void setRevocationReasonCode(String revocationReasonCode) {
        this.revocationReasonCode = revocationReasonCode;
    }

    public String getRevocationReasonDetail() {
        return revocationReasonDetail;
    }

    public void setRevocationReasonDetail(String revocationReasonDetail) {
        this.revocationReasonDetail = revocationReasonDetail;
    }

    public String getRevokedBy() {
        return revokedBy;
    }

    public void setRevokedBy(String revokedBy) {
        this.revokedBy = revokedBy;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }
}