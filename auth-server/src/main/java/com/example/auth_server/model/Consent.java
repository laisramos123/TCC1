package com.example.auth_server.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "consents")
public class Consent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String consentId; // Identificador único do consentimento

    @Column(nullable = false)
    private String userId; // Usuário que deu o consentimento

    @Column(name = "client_id", nullable = false) // ✅ Mapeamento correto para snake_case no DB
    private String clientId; // ✅ Mudança: agora usa camelCase em Java

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    private LocalDateTime revokedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "consent_permissions", joinColumns = @JoinColumn(name = "consent_id"))
    @Column(name = "permission")
    private Set<String> permissions = new HashSet<>(); // Permissões concedidas

    @Column(nullable = false)
    private String status; // AWAITING_AUTHORIZATION, AUTHORIZED, REJECTED, REVOKED

    // ✅ Construtor padrão
    public Consent() {
    }

    // ✅ Construtor com parâmetros principais
    public Consent(String consentId, String userId, String clientId, Set<String> permissions, String status) {
        this.consentId = consentId;
        this.userId = userId;
        this.clientId = clientId;
        this.permissions = permissions != null ? permissions : new HashSet<>();
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    // ✅ Métodos auxiliares para compatibilidade (se necessário)
    @Deprecated
    public String getClient_id() {
        return this.clientId;
    }

    @Deprecated
    public void setClient_id(String client_id) {
        this.clientId = client_id;
    }
}