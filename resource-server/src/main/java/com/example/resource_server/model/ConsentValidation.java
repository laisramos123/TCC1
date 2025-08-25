package com.example.resource_server.model;

import java.time.LocalDateTime;
import java.util.HashSet;
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

import com.example.resource_server.enums.ConsentValidationStatus;
import com.example.resource_server.enums.Permission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "consent_validation", indexes = {
        @Index(name = "idx_validation_status", columnList = "status"),
        @Index(name = "idx_validation_client_id", columnList = "client_id"),
        @Index(name = "idx_validation_expiration", columnList = "expiration_date_time")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentValidation {

    @Id
    @Column(name = "consent_id", length = 256)
    private String consentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ConsentValidationStatus status;

    @Column(name = "expiration_date_time", nullable = false)
    private LocalDateTime expirationDateTime;

    @Column(name = "client_id", nullable = false, length = 100)
    private String clientId;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @OneToMany(mappedBy = "consentValidation", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private Set<ConsentValidationPermission> permissions = new HashSet<>();

    public boolean isValid() {
        return status == ConsentValidationStatus.AUTHORISED &&
                expirationDateTime.isAfter(LocalDateTime.now());
    }

    public boolean hasPermission(Permission permission) {
        return permissions.stream()
                .anyMatch(p -> p.getPermission() == permission);
    }

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}
