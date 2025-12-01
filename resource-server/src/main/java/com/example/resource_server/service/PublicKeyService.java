package com.example.resource_server.service;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class PublicKeyService {

    @Value("${auth-server.url:http://auth-server:8080}")
    private String authServerUrl;

    private final Map<String, PublicKey> publicKeys = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    public void init() {
        // Registrar providers Bouncy Castle
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
            log.info("  BouncyCastle provider registrado");
        }
        if (Security.getProvider("BCPQC") == null) {
            Security.addProvider(new BouncyCastlePQCProvider());
            log.info("  BouncyCastle PQC provider registrado");
        }

        log.info("  Security Providers disponíveis:");
        for (var provider : Security.getProviders()) {
            log.debug("   - {}", provider.getName());
        }

        retryLoadKeys(3, 5000);
    }

    private void retryLoadKeys(int maxRetries, long delayMs) {
        for (int i = 0; i < maxRetries; i++) {
            try {
                if (i > 0) {
                    log.info("  Tentativa {} de {} para carregar chaves...", i + 1, maxRetries);
                    Thread.sleep(delayMs);
                }
                refreshPublicKeys();

                // Verificar se as chaves foram carregadas
                if (publicKeys.containsKey("DILITHIUM") || publicKeys.containsKey("RSA")) {
                    log.info("  Chaves carregadas com sucesso na tentativa {}", i + 1);
                    return;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.warn("  Tentativa {} falhou: {}", i + 1, e.getMessage());
            }
        }
        log.warn("  Não foi possível carregar chaves após {} tentativas", maxRetries);
    }

    @Scheduled(fixedRate = 300000) // Atualiza a cada 5 minutos
    public void refreshPublicKeys() {
        try {
            String url = authServerUrl + "/api/v1/signature/public-keys";
            log.info("🔄 Buscando chaves públicas de: {}", url);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null) {

                loadAndStoreKey(response, "rsa", "RSA");

                loadAndStoreKey(response, "dilithium", "DILITHIUM");
            }
        } catch (Exception e) {
            log.warn("  Não foi possível carregar chaves do auth-server: {}", e.getMessage());
        }
    }

    private void loadAndStoreKey(Map<String, Object> response, String keyName, String algorithm) {
        try {
            if (response.containsKey(keyName)) {
                @SuppressWarnings("unchecked")
                Map<String, String> keyData = (Map<String, String>) response.get(keyName);
                String publicKeyBase64 = keyData.get("publicKey");

                if (publicKeyBase64 != null && !publicKeyBase64.isEmpty()) {
                    PublicKey key = loadPublicKey(publicKeyBase64, algorithm);
                    publicKeys.put(algorithm, key);
                    log.info("  Chave pública {} carregada: {} bytes", algorithm, key.getEncoded().length);
                }
            }
        } catch (Exception e) {
            log.error("  Erro ao carregar chave {}: {}", algorithm, e.getMessage());
            log.debug("Stack trace:", e);
        }
    }

    private PublicKey loadPublicKey(String base64Key, String algorithm) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);

        KeyFactory keyFactory;

        if ("DILITHIUM".equalsIgnoreCase(algorithm)) {

            try {
                keyFactory = KeyFactory.getInstance("Dilithium", "BCPQC");
            } catch (Exception e1) {
                try {
                    keyFactory = KeyFactory.getInstance("DILITHIUM", "BCPQC");
                } catch (Exception e2) {

                    keyFactory = KeyFactory.getInstance("Dilithium");
                }
            }
        } else {
            keyFactory = KeyFactory.getInstance("RSA");
        }

        return keyFactory.generatePublic(keySpec);
    }

    public PublicKey getPublicKey(String algorithm) {
        PublicKey key = publicKeys.get(algorithm.toUpperCase());
        if (key == null) {
            log.warn("  Chave pública não encontrada para algoritmo: {}", algorithm);

            refreshPublicKeys();
            key = publicKeys.get(algorithm.toUpperCase());
        }
        return key;
    }

    public boolean hasKey(String algorithm) {
        return publicKeys.containsKey(algorithm.toUpperCase());
    }

    public Map<String, PublicKey> getAllKeys() {
        return new ConcurrentHashMap<>(publicKeys);
    }
}