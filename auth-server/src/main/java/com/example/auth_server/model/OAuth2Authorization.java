package com.example.auth_server.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "oauth2_authorization", indexes = {
        @Index(name = "idx_oauth2_auth_client_id", columnList = "registered_client_id"),
        @Index(name = "idx_oauth2_auth_consent_id", columnList = "consent_id"),
        @Index(name = "idx_oauth2_auth_principal", columnList = "principal_name"),
        @Index(name = "idx_oauth2_auth_code", columnList = "authorization_code_value"),
        @Index(name = "idx_oauth2_access_token", columnList = "access_token_value")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuth2Authorization {

    @Id
    @Column(name = "id", length = 100)
    private String id;

    @Column(name = "registered_client_id", nullable = false, length = 100)
    private String registeredClientId;

    @Column(name = "principal_name", nullable = false, length = 200)
    private String principalName; // User CPF

    @Column(name = "authorization_grant_type", length = 50)
    private String authorizationGrantType;

    @Lob
    @Column(name = "attributes", columnDefinition = "TEXT")
    private String attributes; // JSON

    @Lob
    @Column(name = "state", columnDefinition = "TEXT")
    private String state;

    // Authorization Code
    @Lob
    @Column(name = "authorization_code_value", columnDefinition = "TEXT")
    private String authorizationCodeValue;

    @Column(name = "authorization_code_issued_at")
    private LocalDateTime authorizationCodeIssuedAt;

    @Column(name = "authorization_code_expires_at")
    private LocalDateTime authorizationCodeExpiresAt;

    // Access Token
    @Lob
    @Column(name = "access_token_value", columnDefinition = "TEXT")
    private String accessTokenValue;

    @Column(name = "access_token_issued_at")
    private LocalDateTime accessTokenIssuedAt;

    @Column(name = "access_token_expires_at")
    private LocalDateTime accessTokenExpiresAt;

    // Refresh Token
    @Lob
    @Column(name = "refresh_token_value", columnDefinition = "TEXT")
    private String refreshTokenValue;

    @Column(name = "refresh_token_issued_at")
    private LocalDateTime refreshTokenIssuedAt;

    @Column(name = "refresh_token_expires_at")
    private LocalDateTime refreshTokenExpiresAt;

    // RELACIONAMENTO CRUCIAL: Liga OAuth2 ao Open Banking Consent
    @Column(name = "consent_id", nullable = false, length = 256)
    private String consentId; // Link to Open Banking Consent

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registered_client_id", insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private OAuth2RegisteredClient registeredClient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consent_id", insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Consent consent;

    // Métodos de conveniência
    public boolean isAuthorizationCodeValid() {
        return authorizationCodeValue != null &&
                authorizationCodeExpiresAt != null &&
                authorizationCodeExpiresAt.isAfter(LocalDateTime.now());
    }

    public boolean isAccessTokenValid() {
        return accessTokenValue != null &&
                accessTokenExpiresAt != null &&
                accessTokenExpiresAt.isAfter(LocalDateTime.now());
    }

    public boolean isRefreshTokenValid() {
        return refreshTokenValue != null &&
                refreshTokenExpiresAt != null &&
                refreshTokenExpiresAt.isAfter(LocalDateTime.now());
    }
}
