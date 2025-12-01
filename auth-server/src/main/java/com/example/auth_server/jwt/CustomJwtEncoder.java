package com.example.auth_server.jwt;

import com.example.auth_server.signature.SignatureAlgorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtEncodingException;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomJwtEncoder implements JwtEncoder {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CustomJwtEncoder.class);

    private final SignatureAlgorithm signatureAlgorithm;
    private final ObjectMapper objectMapper;
    private final String issuer;

    public CustomJwtEncoder(SignatureAlgorithm signatureAlgorithm, String issuer) {
        this.signatureAlgorithm = signatureAlgorithm;
        this.objectMapper = new ObjectMapper();
        this.issuer = issuer;
    }

    @Override
    public Jwt encode(JwtEncoderParameters parameters) throws JwtEncodingException {
        try {
            Map<String, Object> header = new HashMap<>();
            header.put("alg", signatureAlgorithm.getJwtAlgorithmHeader());
            header.put("typ", "JWT");

            String headerJson = objectMapper.writeValueAsString(header);
            String headerBase64 = base64UrlEncode(headerJson);

            Map<String, Object> originalClaims = parameters.getClaims().getClaims();
            Map<String, Object> claims = new HashMap<>();

            for (Map.Entry<String, Object> entry : originalClaims.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if ("scope".equals(key) && value instanceof Collection) {
                    Collection<?> scopes = (Collection<?>) value;
                    String scopeString = scopes.stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(" "));
                    claims.put(key, scopeString);
                } else {
                    claims.put(key, convertToJsonFriendly(value));
                }
            }

            if (!claims.containsKey("iss")) {
                claims.put("iss", issuer);
            }
            if (!claims.containsKey("iat")) {
                claims.put("iat", Instant.now().getEpochSecond());
            }

            String payloadJson = objectMapper.writeValueAsString(claims);
            String payloadBase64 = base64UrlEncode(payloadJson);

            String signingInput = headerBase64 + "." + payloadBase64;

            byte[] signature = signatureAlgorithm.sign(signingInput.getBytes(StandardCharsets.UTF_8));
            String signatureBase64 = base64UrlEncode(signature);

            String tokenValue = signingInput + "." + signatureBase64;

            log.info("JWT assinado com {} - Tamanho: {} bytes",
                    signatureAlgorithm.getAlgorithmName(),
                    tokenValue.length());

            Instant issuedAt = getInstantFromClaim(claims.get("iat"), Instant.now());
            Instant expiresAt = getInstantFromClaim(claims.get("exp"), issuedAt.plusSeconds(3600));

            return new Jwt(
                    tokenValue,
                    issuedAt,
                    expiresAt,
                    header,
                    claims);

        } catch (Exception e) {
            log.error("Error codificando JWT com {}: {}",
                    signatureAlgorithm.getAlgorithmName(),
                    e.getMessage(), e);
            throw new JwtEncodingException("Falha ao codificar JWT: " + e.getMessage(), e);
        }
    }

    private Object convertToJsonFriendly(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Instant) {
            return ((Instant) value).getEpochSecond();
        }

        if (value instanceof URL) {
            return value.toString();
        }

        if (value instanceof Collection) {
            return ((Collection<?>) value).stream()
                    .map(this::convertToJsonFriendly)
                    .collect(Collectors.toList());
        }

        if (value instanceof Map) {
            Map<String, Object> result = new HashMap<>();
            ((Map<?, ?>) value).forEach((k, v) -> {
                result.put(String.valueOf(k), convertToJsonFriendly(v));
            });
            return result;
        }

        return value;
    }

    private Instant getInstantFromClaim(Object value, Instant defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Long) {
            return Instant.ofEpochSecond((Long) value);
        }
        if (value instanceof Integer) {
            return Instant.ofEpochSecond((Integer) value);
        }
        if (value instanceof Instant) {
            return (Instant) value;
        }
        return defaultValue;
    }

    private String base64UrlEncode(String data) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }

    private String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(data);
    }
}
