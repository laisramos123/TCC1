package com.example.auth_server.dilithium;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.bouncycastle.pqc.jcajce.spec.DilithiumParameterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component("dilithiumService")
public class DilithiumSignature {

    private KeyPair keyPair;
    private final DilithiumParameterSpec parameterSpec;
    private static final Logger logger = LoggerFactory.getLogger(DilithiumSignature.class);

    @PostConstruct
    public void init() {
        if (Security.getProvider("BCPQC") == null) {
            Security.addProvider(new BouncyCastleProvider());
            Security.addProvider(new BouncyCastlePQCProvider());
            logger.info("Bouncy Castle providers adicionados.");
        }
    }

    public DilithiumSignature() {
        this(DilithiumParameterSpec.dilithium3);
    }

    public DilithiumSignature(DilithiumParameterSpec paramSpec) {
        this.parameterSpec = paramSpec;
    }

    public KeyPair keyPair() throws Exception {
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

    public byte[] sign(byte[] data) throws Exception {
        if (keyPair == null) {
            keyPair();
        }

        Signature signature = Signature.getInstance("Dilithium", "BCPQC");
        signature.initSign(keyPair.getPrivate());
        signature.update(data);
        return signature.sign();
    }
}
