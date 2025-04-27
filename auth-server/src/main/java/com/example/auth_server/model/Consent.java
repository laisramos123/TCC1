package com.example.auth_server.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Consent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private String ConsentId;
    private String clientId;
    private String scope;
    private String status; // AWAITING_AUTHORIZATION, AUTHORIZED, REVOKED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
