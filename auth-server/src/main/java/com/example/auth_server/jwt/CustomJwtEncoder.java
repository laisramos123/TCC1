package com.example.auth_server.jwt;

import com.example.auth_server.signature.SignatureAlgorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtEncodingException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

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

            Map<String, Object> claims = new HashMap<>(parameters.getClaims().getClaims());

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

            Instant issuedAt = claims.containsKey("iat")
                    ? Instant.ofEpochSecond((Long) claims.get("iat"))
                    : Instant.now();

            Instant expiresAt = claims.containsKey("exp")
                    ? Instant.ofEpochSecond((Long) claims.get("exp"))
                    : issuedAt.plusSeconds(3600);

            return new Jwt(
                    tokenValue,
                    issuedAt,
                    expiresAt,
                    header,
                    claims);

        } catch (Exception e) {
            log.error("Error codificando JWT com {}: {}",
                    signatureAlgorithm.getAlgorithmName(),
                    e.getMessage());
            throw new JwtEncodingException("Falha ao codificar JWT", e);
        }
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
