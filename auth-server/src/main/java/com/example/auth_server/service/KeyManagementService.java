package com.example.auth_server.service;

import com.example.auth_server.signature.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Gerencia chaves criptográficas para RSA e Dilithium
 * Persiste as chaves para que auth-server e resource-server usem as mesmas
 */
@Slf4j
@Service
public class KeyManagementService {

    @Value("${keys.storage.path:/app/keys}")
    private String keysPath;

    @Value("${jwt.signature.algorithm:DILITHIUM}")
    private String activeAlgorithm;

    private final SignatureAlgorithm rsaAlgorithm;
    private final SignatureAlgorithm dilithiumAlgorithm;

    public KeyManagementService(
            @Qualifier("rsaSignature") SignatureAlgorithm rsaAlgorithm,
            @Qualifier("dilithiumAlgorithm") SignatureAlgorithm dilithiumAlgorithm) {
        this.rsaAlgorithm = rsaAlgorithm;
        this.dilithiumAlgorithm = dilithiumAlgorithm;
    }

    @PostConstruct
    public void init() throws Exception {
        log.info("  Inicializando KeyManagementService...");
        log.info("   Algoritmo ativo: {}", activeAlgorithm);
        log.info("   Path das chaves: {}", keysPath);

        Files.createDirectories(Paths.get(keysPath));

        initializeAlgorithm(rsaAlgorithm, "rsa");

        initializeAlgorithm(dilithiumAlgorithm, "dilithium");

        log.info("  Chaves inicializadas com sucesso!");
    }

    private void initializeAlgorithm(SignatureAlgorithm algorithm, String name) throws Exception {
        Path publicKeyPath = Paths.get(keysPath, name + "_public.key");
        Path privateKeyPath = Paths.get(keysPath, name + "_private.key");

        if (Files.exists(publicKeyPath) && Files.exists(privateKeyPath)) {
            log.info("   Carregando chaves {} existentes...", name.toUpperCase());

            algorithm.generateKeyPair();
            saveKeys(algorithm, name);
        } else {
            log.info("   Gerando novas chaves {}...", name.toUpperCase());
            algorithm.generateKeyPair();
            saveKeys(algorithm, name);
        }

        log.info("   {} - Chave pública: {} bytes",
                name.toUpperCase(),
                algorithm.getPublicKey().getEncoded().length);
    }

    private void saveKeys(SignatureAlgorithm algorithm, String name) throws IOException {
        Path publicKeyPath = Paths.get(keysPath, name + "_public.key");

        String publicKeyBase64 = Base64.getEncoder().encodeToString(
                algorithm.getPublicKey().getEncoded());
        Files.writeString(publicKeyPath, publicKeyBase64);

        log.info("   Chave pública {} salva em: {}", name.toUpperCase(), publicKeyPath);
    }

    public String getActiveAlgorithm() {
        return activeAlgorithm;
    }

    public SignatureAlgorithm getActiveSignatureAlgorithm() {
        return "DILITHIUM".equalsIgnoreCase(activeAlgorithm)
                ? dilithiumAlgorithm
                : rsaAlgorithm;
    }

    public SignatureAlgorithm getRsaAlgorithm() {
        return rsaAlgorithm;
    }

    public SignatureAlgorithm getDilithiumAlgorithm() {
        return dilithiumAlgorithm;
    }
}