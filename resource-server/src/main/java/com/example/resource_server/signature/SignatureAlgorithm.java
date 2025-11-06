package com.example.resource_server.signature;

import java.security.PrivateKey;
import java.security.PublicKey;

public interface SignatureAlgorithm {

    String getAlgorithmName();

    void generateKeyPair() throws Exception;

    byte[] sign(byte[] data) throws Exception;

    boolean verify(byte[] data, byte[] signature, PublicKey publicKey) throws Exception;

    PublicKey getPublicKey();

    PrivateKey getPrivateKey();

    String getJwtAlgorithmHeader();

    SignatureMetrics getMetrics();
}