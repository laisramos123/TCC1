package com.example.auth_server.dilithium;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.bouncycastle.pqc.jcajce.spec.DilithiumParameterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import jakarta.annotation.PostConstruct;

public class DilithiumSignature {

    private KeyPair keyPair;
    private final DilithiumParameterSpec parameterSpec;
    private static final Logger logger = LoggerFactory.getLogger(DilithiumSignature.class);

    @PostConstruct
    public void init() {
        if (Security.getProvider("BCPQC") == null) {
            Security.addProvider(new BouncyCastleProvider());
            Security.addProvider(new BouncyCastlePQCProvider());
            logger.info("Bouncy Castle providers added.");
        } else {
            logger.debug("📋 Bouncy Castle providers já estavam carregados");
        }
    }

    public DilithiumSignature() {
        this(DilithiumParameterSpec.dilithium3);
    }

    public DilithiumSignature(DilithiumParameterSpec paramSpec) {

        this.parameterSpec = paramSpec;
    }

    public KeyPair keyPair()
            throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {

        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("Dilithium", "BCPQC");
        keyPairGen.initialize(parameterSpec, new SecureRandom());
        return this.keyPair = keyPairGen.generateKeyPair();

    }

    public boolean verify(byte[] data, byte[] signatureBytes, PublicKey publicKey) throws Exception {
        Signature signature = Signature.getInstance("Dilithium", "BCPQC");
        signature.initVerify(publicKey);
        signature.update(data);
        return signature.verify(signatureBytes);
    }

    public PublicKey getPublicKey() {
        return keyPair != null ? keyPair.getPublic() : null;
    }

    public PrivateKey getPrivateKey() {
        return keyPair != null ? keyPair.getPrivate() : null;
    }

    public DilithiumParameterSpec getParameterSpec() {
        return parameterSpec;
    }

    public void printKeysAndSaveBase64() {
        if (keyPair == null) {
            logger.warn("Chave não gerada. Chame keyPair() primeiro.");
            return;
        }
        String publicKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        String privateKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

        logger.info("Public Key (Base64): " + publicKeyBase64);
        logger.info("Private Key (Base64): " + privateKeyBase64);

        logger.info("Private Key (Base64) Size: " + privateKeyBase64.length());
        logger.info("Public Key (Base64) Size: " + publicKeyBase64.length());
    }

    public PrivateKey loadPrivateKeyFromBase64(String base64Key) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("Dilithium", "BCPQC");
        return keyFactory.generatePrivate(keySpec);
    }

    public PublicKey loadPublicKeyFromBase64(String base64Key) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("Dilithium", "BCPQC");
        return keyFactory.generatePublic(keySpec);
    }

    public static class SecurityLevels {

        public static DilithiumSignature level3() {
            return new DilithiumSignature(DilithiumParameterSpec.dilithium3);
        }

    }

    public byte[] sign(byte[] data) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sign'");
    }
}
