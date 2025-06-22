package com.example.resource_server.model;

import java.time.LocalDateTime;
import java.util.Set;

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

    @Column(unique = true)
    private String consentId; // Identificador único do consentimento

    private String userId; // Usuário que deu o consentimento

    private String clientId; // TPP que recebeu o consentimento

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    private LocalDateTime revokedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> permissions; // Permissões concedidas (accounts, transactions, etc)

    private String status; // AWAITING_AUTHORIZATION, AUTHORIZED, REJECTED, REVOKED
}
