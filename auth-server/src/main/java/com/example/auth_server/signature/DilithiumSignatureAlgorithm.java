package com.example.auth_server.signature;

import org.bouncycastle.pqc.jcajce.spec.DilithiumParameterSpec;
import org.springframework.stereotype.Component;
import java.security.*;

@Component("dilithiumSignature")
public class DilithiumSignatureAlgorithm implements SignatureAlgorithm {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DilithiumSignatureAlgorithm.class);

    private KeyPair keyPair;
    private final SignatureMetrics metrics = new SignatureMetrics();

    @Override
    public String getAlgorithmName() {
        return "DILITHIUM";
    }

    @Override
    public void generateKeyPair() throws Exception {
        long startTime = System.nanoTime();

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("Dilithium", "BCPQC");
        keyGen.initialize(DilithiumParameterSpec.dilithium3, new SecureRandom());
        this.keyPair = keyGen.generateKeyPair();

        long duration = System.nanoTime() - startTime;
        metrics.setKeyGenerationTime(duration / 1_000_000);

        log.info("✅ Dilithium3 keypair generated in {} ms", metrics.getKeyGenerationTime());
    }

    @Override
    public byte[] sign(byte[] data) throws Exception {
        if (keyPair == null) {
            generateKeyPair();
        }

        long startTime = System.nanoTime();

        Signature signature = Signature.getInstance("Dilithium", "BCPQC");
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

        Signature signature = Signature.getInstance("Dilithium", "BCPQC");
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
        return "DILITHIUM3";
    }

    @Override
    public SignatureMetrics getMetrics() {
        return metrics;
    }
}
