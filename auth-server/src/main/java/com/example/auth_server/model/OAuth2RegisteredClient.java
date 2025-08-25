package com.example.auth_server.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "oauth2_registered_client", indexes = {
        @Index(name = "idx_oauth2_client_id", columnList = "client_id", unique = true),
        @Index(name = "idx_oauth2_org_id", columnList = "org_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuth2RegisteredClient {

    @Id
    @Column(name = "id", length = 100)
    private String id;

    @Column(name = "client_id", unique = true, nullable = false, length = 100)
    private String clientId;

    @Column(name = "client_name", length = 200)
    private String clientName;

    @Column(name = "client_secret", length = 200)
    private String clientSecret;

    @Column(name = "client_id_issued_at")
    private LocalDateTime clientIdIssuedAt;

    @Column(name = "client_secret_expires_at")
    private LocalDateTime clientSecretExpiresAt;

    @Lob
    @Column(name = "software_statement", columnDefinition = "TEXT")
    private String softwareStatement; // JWT from DCR

    @Column(name = "org_id", length = 100)
    private String orgId;

    @Column(name = "software_id", length = 100)
    private String softwareId;

    @OneToMany(mappedBy = "registeredClient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OAuth2Authorization> authorizations = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (clientIdIssuedAt == null) {
            clientIdIssuedAt = LocalDateTime.now();
        }
    }
}
