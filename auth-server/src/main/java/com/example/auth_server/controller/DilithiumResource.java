package com.example.auth_server.controller;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.auth_server.dilithium.DilithiumSignature;
import com.example.auth_server.dto.VerificationRequestDTO;

@RestController
@RequestMapping("/api/v1/dilithium")
public class DilithiumResource {

    @Autowired
    private DilithiumSignature dilithiumSignature;

    @PostMapping("/public/assinar")
    public ResponseEntity<Map<String, Object>> assinarDados(
            @RequestBody SignRequestDTO request) {

        Map<String, Object> response = new HashMap<>();

        try {
            if (dilithiumSignature.getPublicKey() == null) {
                dilithiumSignature.keyPair();
            }

            byte[] data = request.getData().getBytes(StandardCharsets.UTF_8);
            byte[] signature = dilithiumSignature.sign(data);

            response.put("signature", Base64.getEncoder().encodeToString(signature));
            response.put("publicKey",
                    Base64.getEncoder().encodeToString(
                            dilithiumSignature.getPublicKey().getEncoded()));
            response.put("algorithm", "Dilithium3");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Erro ao assinar: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/public/verificar")
    public ResponseEntity<Map<String, Object>> verificarAssinatura(
            @RequestBody VerificationRequestDTO request) {

        Map<String, Object> response = new HashMap<>();

        try {
            byte[] data = Base64.getDecoder().decode(request.getData());
            byte[] signature = Base64.getDecoder().decode(request.getSignature());

            PublicKey publicKey = dilithiumSignature
                    .loadPublicKeyFromBase64(request.getPublicKey());

            boolean isValid = dilithiumSignature.verify(data, signature, publicKey);

            response.put("valid", isValid);
            response.put("algorithm", "Dilithium3");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Erro ao verificar: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getAlgorithmInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("algorithm", "Dilithium");
        info.put("securityLevel", "192-bit (Level 3)");
        info.put("quantumResistant", true);
        info.put("standardized", "NIST PQC Standard");
        return ResponseEntity.ok(info);
    }
}
