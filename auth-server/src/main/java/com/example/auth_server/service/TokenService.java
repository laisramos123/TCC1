package com.example.auth_server.service;

import java.security.Key;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.auth_server.model.AuthorizationCode;
import com.example.auth_server.model.Consent;
import com.example.auth_server.model.TokenResponse;
import com.example.auth_server.repository.AuthorizationCodeRepository;
import com.example.auth_server.repository.ConsentRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class TokenService {

    @Autowired
    private AuthorizationCodeRepository authorizationCodeRepository;
    @Autowired
    private ConsentRepository consentRepository;

    @Autowired
    private Key signingKey;

    public TokenResponse generateToken(String clientId, String redirectUri, String code) {
        AuthorizationCode authorizationCode = authorizationCodeRepository.findByAuthorizationCode(code)
                .orElseThrow(() -> new RuntimeException("Invalid authorization code"));

        if (authorizationCode.isExpired()) {
            throw new RuntimeException("Authorization code expired");
        }

        if (!authorizationCode.getClientId().equals(clientId)) {
            throw new RuntimeException("Invalid client ID");
        }

        Consent consent = consentRepository.findByConsentId(authorizationCode.getConsent().getConsentId())
                .orElseThrow(() -> new RuntimeException("Invalid consent ID"));

        String accessToken = generateAccessToken(clientId, authorizationCode.getUserId(), consent);
        String refreshToken = generateRefreshToken(clientId, authorizationCode.getUserId(), consent.getConsentId());

        String idToken = null;
        if (consent.getScope().contains("openid")) {
            idToken = generateIdToken(clientId, authorizationCode.getUserId());
        }

        authorizationCode.setUsed(true);
        authorizationCodeRepository.save(authorizationCode);

        return new TokenResponse(
                accessToken,
                "Bearer",
                3600,
                refreshToken,
                idToken);

    }

    private String generateIdToken(String clientId, String userId) {
        Date now = new Date(System.currentTimeMillis());
        Date expiryDate = new Date(now.getTime() + 3600 * 1000); // 1 hour expiration

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userId);
        claims.put("aud", clientId);
        claims.put("iss", "https://example.com"); // Issuer URL
        claims.put("iat", now.getTime() / 1000L);
        claims.put("exp", expiryDate.getTime() / 1000L);

        return Jwts.builder()
                .setClaims(claims)
                .signWith(signingKey, SignatureAlgorithm.PS256)
                .compact();
    }

    private String generateRefreshToken(String clientId, String redirectUri, String scope) {
        Date now = new Date(System.currentTimeMillis());
        Date expiryDate = new Date(now.getTime() + 30 * 24 * 3600 * 1000); // 30 days expiration

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", clientId);
        claims.put("aud", redirectUri);
        claims.put("scope", scope);
        claims.put("iss", "https://example.com"); // Issuer URL
        claims.put("iat", now.getTime() / 1000L);
        claims.put("exp", expiryDate.getTime() / 1000L);

        return Jwts.builder()
                .setClaims(claims)
                .signWith(signingKey, SignatureAlgorithm.PS256)
                .compact();
    }

    private String generateAccessToken(String clientId, String userId, Consent consent) {
        Date now = new Date(System.currentTimeMillis());
        Date expiryDate = new Date(now.getTime() + 3600 * 1000); // 1 hour expiration

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userId);
        claims.put("consentid", consent.getConsentId());
        claims.put("scope", String.join(" ", consent.getScope()));

        return Jwts.builder()
                .setClaims(claims)
                .setIssuer("https://example.com") // Issuer URL1
                .setAudience(userId)
                .setExpiration(expiryDate)
                .setIssuedAt(now)
                .setId(UUID.randomUUID().toString())
                .signWith(signingKey, SignatureAlgorithm.PS256)
                .compact();

    }

}
