package com.example.auth_server.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthorizationCode {
    private String code;
    private String clientId;
    private String redirectUri;
    private String scope;
    private String state;
    private long expiresIn;
    private String userId;
    private long createdAt;
    private Consent consent;
    private boolean used;

    public AuthorizationCode(String code, String clientId, String redirectUri, String scope, String state,
            long expiresIn) {
        this.code = code;
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.scope = scope;
        this.state = state;
        this.expiresIn = expiresIn;
        this.userId = null;
        this.consent = null;
        this.createdAt = System.currentTimeMillis();
        this.used = false;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > (createdAt + expiresIn * 1000);
    }
}
