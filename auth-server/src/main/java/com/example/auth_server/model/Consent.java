package com.example.auth_server.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.auth_server.enums.ConsentStatus;

@Entity
@Table(name = "consents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Consent {

    @Id
    private String consentId;

    @Column(nullable = false)
    private String loggedUserDocument;

    @Column(nullable = false)
    private String loggedUserRel;

    @Column(nullable = false)
    private String businessEntityDocument;

    @Column(nullable = false)
    private String businessEntityRel;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "consent_permissions", joinColumns = @JoinColumn(name = "consent_id"))
    @Column(name = "permission")
    private List<String> permissions = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ConsentStatus status;
    @Column(nullable = false)
    private LocalDateTime creationDateTime;

    @Column(nullable = false)
    private LocalDateTime statusUpdateDateTime;

    @Column(nullable = false)
    private LocalDateTime expirationDateTime;

    @Column(nullable = false)
    private LocalDateTime transactionFromDateTime;

    @Column(nullable = false)
    private LocalDateTime transactionToDateTime;

    private String rejectionReasonCode;
    private String rejectionReasonDetail;

    private String revocationReasonCode;
    private String revocationReasonDetail;
    private String revokedBy;
    private LocalDateTime revokedAt;

    @PrePersist
    protected void onCreate() {
        if (creationDateTime == null) {
            creationDateTime = LocalDateTime.now();
        }
        if (statusUpdateDateTime == null) {
            statusUpdateDateTime = LocalDateTime.now();
        }
        if (status == null) {
            status = ConsentStatus.AWAITING_AUTHORISATION;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        statusUpdateDateTime = LocalDateTime.now();
    }
}
