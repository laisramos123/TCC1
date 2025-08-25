package com.example.auth_server.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;

import com.example.auth_server.enums.ConsentStatus;
import com.example.auth_server.enums.Permission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "consents", indexes = {
        @Index(name = "idx_consent_client_id", columnList = "client_id"),
        @Index(name = "idx_consent_status", columnList = "status"),
        @Index(name = "idx_consent_expiration", columnList = "expiration_date_time")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consent {
    @Id
    @Column(name = "consent_id", length = 256)
    private String consentId; // URN: urn:banco:C1DD33123

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ConsentStatus status;

    @Column(name = "creation_date_time", nullable = false)
    private LocalDateTime creationDateTime;

    @Column(name = "status_update_date_time", nullable = false)
    private LocalDateTime statusUpdateDateTime;

    @Column(name = "expiration_date_time", nullable = false)
    private LocalDateTime expirationDateTime;

    @Column(name = "transaction_from_date_time")
    private LocalDateTime transactionFromDateTime;

    @Column(name = "transaction_to_date_time")
    private LocalDateTime transactionToDateTime;

    @Column(name = "client_id", nullable = false)
    private String clientId; // TPP client identifier

    @Column(name = "interaction_id")
    private String interactionId; // x-fapi-interaction-id

    // Embedded User Documents
    @Column(name = "logged_user_document", length = 11)
    private String loggedUserDocument; // CPF - 11 digits

    @Column(name = "logged_user_document_type", length = 3)
    private String loggedUserDocumentType; // CPF

    @Column(name = "business_entity_document", length = 14)
    private String businessEntityDocument; // CNPJ - 14 digits

    @Column(name = "business_entity_document_type", length = 4)
    private String businessEntityDocumentType; // CNPJ

    // Relacionamentos
    @OneToMany(mappedBy = "consent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ConsentPermission> permissions = new HashSet<>();

    @OneToMany(mappedBy = "consent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ConsentAudit> auditLog = new ArrayList<>();

    @OneToMany(mappedBy = "consent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OAuth2Authorization> authorizations = new ArrayList<>();

    public void addPermission(Permission permission) {
        ConsentPermission cp = new ConsentPermission();
        cp.setConsentId(this.consentId);
        cp.setPermission(permission);
        cp.setConsent(this);
        this.permissions.add(cp);
    }

    public void addAuditEntry(ConsentStatus previousStatus, ConsentStatus newStatus, String reason, String userAgent,
            String ipAddress) {
        ConsentAudit audit = ConsentAudit.builder()
                .consentId(this.consentId)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .timestamp(LocalDateTime.now())
                .reason(reason)
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .consent(this)
                .build();
        this.auditLog.add(audit);
    }

    @PrePersist
    protected void onCreate() {
        if (creationDateTime == null) {
            creationDateTime = LocalDateTime.now();
        }
        if (statusUpdateDateTime == null) {
            statusUpdateDateTime = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        statusUpdateDateTime = LocalDateTime.now();
    }
}
