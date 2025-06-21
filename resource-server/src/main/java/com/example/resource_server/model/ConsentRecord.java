package com.example.resource_server.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "consent_records")
@Getter
@Setter
public class ConsentRecord {

    @Id
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @ElementCollection
    @CollectionTable(name = "consent_scopes", joinColumns = @JoinColumn(name = "consent_id"))
    @Column(name = "scope")
    private List<String> scopes = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(nullable = false)
    private String status; // ACTIVE, EXPIRED, REVOKED

    // Para rastreamento de uso
    @OneToMany(mappedBy = "consent", cascade = CascadeType.ALL)
    private List<ConsentAccess> accessLogs = new ArrayList<>();
}