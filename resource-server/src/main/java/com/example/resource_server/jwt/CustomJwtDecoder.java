package com.example.resource_server.jwt;

import com.example.resource_server.signature.SignatureAlgorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Slf4j
public class CustomJwtDecoder implements JwtDecoder {

    private final SignatureAlgorithm signatureAlgorithm;
    private final ObjectMapper objectMapper;

    public CustomJwtDecoder(SignatureAlgorithm signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        try {

            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new JwtException("Invalid JWT format");
            }

            String headerBase64 = parts[0];
            String payloadBase64 = parts[1];
            String signatureBase64 = parts[2];

            String headerJson = new String(base64UrlDecode(headerBase64), StandardCharsets.UTF_8);
            String payloadJson = new String(base64UrlDecode(payloadBase64), StandardCharsets.UTF_8);

            @SuppressWarnings("unchecked")
            Map<String, Object> header = objectMapper.readValue(headerJson, Map.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> claims = objectMapper.readValue(payloadJson, Map.class);

            String signingInput = headerBase64 + "." + payloadBase64;
            byte[] signatureBytes = base64UrlDecode(signatureBase64);

            boolean isValid = signatureAlgorithm.verify(
                    signingInput.getBytes(StandardCharsets.UTF_8),
                    signatureBytes,
                    signatureAlgorithm.getPublicKey());

            if (!isValid) {
                throw new JwtException("Invalid JWT signature - " + signatureAlgorithm.getAlgorithmName());
            }

            log.info("  JWT verified with {} - Algorithm: {}",
                    signatureAlgorithm.getAlgorithmName(),
                    header.get("alg"));

            Instant issuedAt = claims.containsKey("iat")
                    ? Instant.ofEpochSecond(((Number) claims.get("iat")).longValue())
                    : Instant.now();

            Instant expiresAt = claims.containsKey("exp")
                    ? Instant.ofEpochSecond(((Number) claims.get("exp")).longValue())
                    : issuedAt.plusSeconds(3600);

            // 5. Validate expiration
            if (Instant.now().isAfter(expiresAt)) {
                throw new JwtException("JWT token expired");
            }

            return new Jwt(token, issuedAt, expiresAt, header, claims);

        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            log.error("  Error decoding JWT with {}: {}",
                    signatureAlgorithm.getAlgorithmName(),
                    e.getMessage());
            throw new JwtException("Failed to decode JWT", e);
        }
    }

    private byte[] base64UrlDecode(String data) {
        return Base64.getUrlDecoder().decode(data);
    }
}
