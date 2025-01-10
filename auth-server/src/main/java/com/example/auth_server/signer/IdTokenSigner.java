package com.example.auth_server.signer;

import org.bouncycastle.pqc.jcajce.interfaces.DilithiumPrivateKey;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.stereotype.Component;

import java.security.*;
import java.util.Date;
import java.util.UUID;

@Component
public class IdTokenSigner {
     

    private final DilithiumPrivateKey privateKey;

    public IdTokenSigner(DilithiumPrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public String generateSignedIdToken(String issuer, String subject, String audience) throws Exception {
        // Cria conjunto de declarações JWT
        String claims = "{" +
                "\"iss\": \"" + issuer + "\"," +
                "\"sub\": \"" + subject + "\"," +
                "\"aud\": \"" + audience + "\"," +
                "\"exp\": " + (System.currentTimeMillis() / 1000 + 3600) + "," +
                "\"iat\": " + (System.currentTimeMillis() / 1000) + "," +
                "\"jti\": \"" + UUID.randomUUID().toString() + "\"}";

        // Cria assinatura
        Signature signature = Signature.getInstance("Dilithium", "BCPQC");
        signature.initSign(privateKey);
        signature.update(claims.getBytes());
        byte[] signatureBytes = signature.sign();

        // Codifica o ID Token
        String encodedClaims = Base64.toBase64String(claims.getBytes());
        String encodedSignature = Base64.toBase64String(signatureBytes);

        return encodedClaims + "." + encodedSignature;
    }
}
