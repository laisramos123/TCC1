package com.example.auth_server.dilithium;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.bouncycastle.pqc.jcajce.spec.DilithiumParameterSpec;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DilithiumKeyGeneratorService {
    static {
        // Registrar o provider do Bouncy Castle para algoritmos pós-quânticos
        Security.addProvider(new BouncyCastlePQCProvider());
    }

    /**
     * Gera um par de chaves Dilithium
     * 
     * @return KeyPair com chave pública e privada Dilithium
     */
    public KeyPair generateDilithiumKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("Dilithium", "BCPQC");
            // Usar Dilithium3 que oferece segurança de 128 bits
            keyGen.initialize(DilithiumParameterSpec.dilithium3, new SecureRandom());

            KeyPair keyPair = keyGen.generateKeyPair();
            log.info("✅ Par de chaves Dilithium gerado com sucesso");
            log.debug("Algoritmo: {}", keyPair.getPrivate().getAlgorithm());
            log.debug("Formato chave privada: {}", keyPair.getPrivate().getFormat());

            return keyPair;
        } catch (Exception e) {
            log.error("❌ Erro ao gerar chaves Dilithium", e);
            throw new RuntimeException("Falha ao gerar chaves Dilithium", e);
        }
    }

    /**
     * Cria um JWK customizado para Dilithium
     */
    public DilithiumJWK createDilithiumJWK(KeyPair keyPair) {
        String keyId = UUID.randomUUID().toString();

        // Codificar as chaves em Base64
        String publicKeyBase64 = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(keyPair.getPublic().getEncoded());

        String privateKeyBase64 = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(keyPair.getPrivate().getEncoded());

        return new DilithiumJWK(keyId, publicKeyBase64, privateKeyBase64);
    }

    /**
     * Classe para representar uma chave JWK Dilithium
     */
    public static class DilithiumJWK {
        private final String kid;
        private final String publicKey;
        private final String privateKey;
        private final String kty = "PQC"; // Key type para algoritmos pós-quânticos
        private final String alg = "Dilithium3"; // Algoritmo específico
        private final String use = "sig"; // Uso para assinatura

        public DilithiumJWK(String kid, String publicKey, String privateKey) {
            this.kid = kid;
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }

        // Getters
        public String getKid() {
            return kid;
        }

        public String getPublicKey() {
            return publicKey;
        }

        public String getPrivateKey() {
            return privateKey;
        }

        public String getKty() {
            return kty;
        }

        public String getAlg() {
            return alg;
        }

        public String getUse() {
            return use;
        }

        /**
         * Converte para formato JSON
         */
        public Map<String, Object> toJSONObject() {
            Map<String, Object> json = new HashMap<>();
            json.put("kty", kty);
            json.put("kid", kid);
            json.put("alg", alg);
            json.put("use", use);
            json.put("x", publicKey); // Chave pública
            if (privateKey != null) {
                json.put("d", privateKey); // Chave privada
            }
            return json;
        }
    }

}
