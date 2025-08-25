package com.example.auth_client.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "client_token_cache")
public class ClientTokenCache {
    @Id
    @Column(name = "access_token")
    private String accessToken;

    @Column(name = "consent_id", nullable = false)
    private String consentId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "scopes_json", columnDefinition = "TEXT")
    private String scopesJson;

    @Column(name = "cached_at", nullable = false)
    private LocalDateTime cachedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consent_id", insertable = false, updatable = false)
    private ClientConsentCache clientConsentCache;

    // Getters, Setters, Constructors...
}
