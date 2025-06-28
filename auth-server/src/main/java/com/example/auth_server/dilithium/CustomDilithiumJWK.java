package com.example.auth_server.dilithium;

import java.net.URI;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.bouncycastle.pqc.jcajce.spec.DilithiumParameterSpec;

import com.example.auth_server.dilithium.DilithiumKeyGeneratorService.DilithiumJWK;
import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyOperation;
import com.nimbusds.jose.jwk.KeyType;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.util.Base64URL;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomDilithiumJWK extends JWK {

    protected CustomDilithiumJWK(KeyType kty, KeyUse use, Set<KeyOperation> ops, Algorithm alg, String kid, URI x5u,
            Base64URL x5t, Base64URL x5t256, List<com.nimbusds.jose.util.Base64> x5c, Date exp, Date nbf, Date iat,
            KeyStore ks) {
        super(kty, use, ops, alg, kid, x5u, x5t, x5t256, x5c, exp, nbf, iat, ks);

    }

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

        return new DilithiumJWK(keyPair, keyId, publicKeyBase64, privateKeyBase64);
    }

    /**
     * Classe para representar uma chave JWK Dilithium que estende JWK
     */
    public static class DilithiumJWK extends JWK {
        private final String publicKeyBase64;
        private final String privateKeyBase64;
        private final KeyPair keyPair;

        // KeyType customizado para algoritmos pós-quânticos
        private static final KeyType PQC_KEY_TYPE = new KeyType("PQC", null);

        public DilithiumJWK(KeyPair keyPair, String kid, String publicKeyBase64, String privateKeyBase64) {
            super(
                    PQC_KEY_TYPE, // KeyType (não String!)
                    KeyUse.SIGNATURE, // use
                    Set.of(KeyOperation.SIGN, KeyOperation.VERIFY), // ops
                    new Algorithm("Dilithium3"), // alg
                    kid, // kid
                    null, // x5u
                    null, // x5t
                    null, // x5t256
                    null, // x5c
                    null, // exp
                    null, // nbf
                    new Date(), // iat
                    null // keyStore
            );

            this.keyPair = keyPair;
            this.publicKeyBase64 = publicKeyBase64;
            this.privateKeyBase64 = privateKeyBase64;
        }

        @Override
        public LinkedHashMap<String, ?> getRequiredParams() {
            LinkedHashMap<String, Object> params = new LinkedHashMap<>();
            params.put("kty", getKeyType().getValue());
            params.put("alg", "Dilithium3");
            params.put("x", publicKeyBase64); // Chave pública
            return params;
        }

        @Override
        public boolean isPrivate() {
            return privateKeyBase64 != null && keyPair.getPrivate() != null;
        }

        @Override
        public JWK toPublicJWK() {
            // Retorna uma versão sem a chave privada
            return new DilithiumJWK(
                    new KeyPair(keyPair.getPublic(), null),
                    getKeyID(),
                    publicKeyBase64,
                    null);
        }

        @Override
        public int size() {
            // Dilithium3 oferece segurança equivalente a 128 bits
            return 128;
        }

        // Getters específicos
        public String getPublicKeyBase64() {
            return publicKeyBase64;
        }

        public String getPrivateKeyBase64() {
            return privateKeyBase64;
        }

        public KeyPair getKeyPair() {
            return keyPair;
        }

        /**
         * Converte para formato JSON customizado
         */
        @Override
        public Map<String, Object> toJSONObject() {
            Map<String, Object> json = super.toJSONObject();

            // Adicionar campos específicos do Dilithium
            json.put("x", publicKeyBase64); // Chave pública

            if (isPrivate()) {
                json.put("d", privateKeyBase64); // Chave privada
            }

            return json;
        }

        /**
         * Método utilitário para obter a chave pública Java
         */
        public PublicKey getPublicKey() {
            return keyPair.getPublic();
        }

        /**
         * Método utilitário para obter a chave privada Java
         */
        public PrivateKey getPrivateKey() {
            return keyPair.getPrivate();
        }
    }

    @Override
    public LinkedHashMap<String, ?> getRequiredParams() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRequiredParams'");
    }

    @Override
    public boolean isPrivate() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isPrivate'");
    }

    @Override
    public JWK toPublicJWK() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toPublicJWK'");
    }

    @Override
    public int size() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'size'");
    }
}
