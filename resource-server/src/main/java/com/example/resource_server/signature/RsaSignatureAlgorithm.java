package com.example.resource_server.signature;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.*;

@Slf4j
@Component("rsaSignature")
public class RsaSignatureAlgorithm implements SignatureAlgorithm {

    private KeyPair keyPair;
    private final SignatureMetrics metrics = new SignatureMetrics();

    @Override
    public String getAlgorithmName() {
        return "RSA";
    }

    @Override
    public void generateKeyPair() throws Exception {
        long startTime = System.nanoTime();

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048, new SecureRandom());
        this.keyPair = keyGen.generateKeyPair();

        long duration = System.nanoTime() - startTime;
        metrics.setKeyGenerationTime(duration / 1_000_000);

        log.info(" RSA-2048 keypair generated in {} ms", metrics.getKeyGenerationTime());
    }

    @Override
    public byte[] sign(byte[] data) throws Exception {
        if (keyPair == null) {
            generateKeyPair();
        }

        long startTime = System.nanoTime();

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(keyPair.getPrivate());
        signature.update(data);
        byte[] signatureBytes = signature.sign();

        long duration = System.nanoTime() - startTime;
        metrics.recordSignOperation(duration / 1_000_000);

        return signatureBytes;
    }

    @Override
    public boolean verify(byte[] data, byte[] signatureBytes, PublicKey publicKey) throws Exception {
        long startTime = System.nanoTime();

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(data);
        boolean isValid = signature.verify(signatureBytes);

        long duration = System.nanoTime() - startTime;
        metrics.recordVerifyOperation(duration / 1_000_000);

        return isValid;
    }

    @Override
    public PublicKey getPublicKey() {
        return keyPair != null ? keyPair.getPublic() : null;
    }

    @Override
    public PrivateKey getPrivateKey() {
        return keyPair != null ? keyPair.getPrivate() : null;
    }

    @Override
    public String getJwtAlgorithmHeader() {
        return "RS256";
    }

    @Override
    public SignatureMetrics getMetrics() {
        return metrics;
    }
}