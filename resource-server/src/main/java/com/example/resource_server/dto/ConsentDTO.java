package com.example.resource_server.dto;

import java.time.LocalDateTime;
import java.util.Set;

import lombok.Data;

@Data
public class ConsentDTO {
    private String id;
    private String clientId;
    private String clientName;
    private Set<String> scopes;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private String status; // ACTIVE, EXPIRED, REVOKED
    private LocalDateTime revokedAt;
    private String logoUri; // URL do logo do cliente (para UI)
}
