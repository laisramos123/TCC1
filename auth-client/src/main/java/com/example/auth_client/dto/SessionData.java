package com.example.auth_client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionData {

    private String consentId;
    private String codeVerifier;
    private String state;
    private String accessToken;
    private String refreshToken;
    private Instant tokenExpiresAt;
    private String userCpf;
    private String userName;
}