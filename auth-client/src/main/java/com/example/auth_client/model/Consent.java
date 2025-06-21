package com.example.auth_client.model;

import java.time.LocalDateTime;
import java.util.Set;

import lombok.Data;

@Data
public class Consent {
    private Long id;
    private String consentId;
    private String userId;
    private String clientId;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime revokedAt;
    private Set<String> permissions;
    private String status;
}
