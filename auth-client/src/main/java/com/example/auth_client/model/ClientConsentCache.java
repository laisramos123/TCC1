package com.example.auth_client.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.example.auth_client.enums.ConsentStatus;

@Entity
@Table(name = "client_consent_cache")
public class ClientConsentCache {
    @Id
    @Column(name = "consent_id")
    private String consentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ConsentStatus status;

    @Column(name = "permissions_json", columnDefinition = "TEXT")
    private String permissionsJson;

    @Column(name = "expiration_date_time", nullable = false)
    private LocalDateTime expirationDateTime;

    @Column(name = "cached_at", nullable = false)
    private LocalDateTime cachedAt;

    @OneToMany(mappedBy = "clientConsentCache", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ClientTokenCache> tokens = new ArrayList<>();

    // Getters, Setters, Constructors...
}
