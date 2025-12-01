package com.example.auth_server.controller;

import com.example.auth_server.signature.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/jwt")
public class JwkController {

    @Value("${jwt.signature.algorithm:RSA}")
    private String algorithmName;

    private final SignatureAlgorithm rsaSignature;
    private final SignatureAlgorithm dilithiumSignature;

    public JwkController(
            @Qualifier("rsaSignature") SignatureAlgorithm rsaSignature,
            @Qualifier("dilithiumAlgorithm") SignatureAlgorithm dilithiumSignature) {
        this.rsaSignature = rsaSignature;
        this.dilithiumSignature = dilithiumSignature;
    }

    @GetMapping("/public-key")
    public ResponseEntity<Map<String, Object>> getPublicKey() {
        Map<String, Object> response = new HashMap<>();

        try {
            SignatureAlgorithm algorithm = "DILITHIUM".equalsIgnoreCase(algorithmName)
                    ? dilithiumSignature
                    : rsaSignature;

            if (algorithm.getPublicKey() == null) {
                algorithm.generateKeyPair();
            }

            response.put("algorithm", algorithm.getAlgorithmName());
            response.put("jwtAlgorithm", algorithm.getJwtAlgorithmHeader());
            response.put("publicKey", Base64.getEncoder().encodeToString(
                    algorithm.getPublicKey().getEncoded()));
            response.put("format", algorithm.getPublicKey().getFormat());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Failed to get public key: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}