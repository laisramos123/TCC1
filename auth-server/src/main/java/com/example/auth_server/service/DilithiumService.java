// package com.example.auth_server.service;

// import java.security.InvalidAlgorithmParameterException;
// import java.security.Key;
// import java.security.KeyPair;
// import java.security.NoSuchAlgorithmException;
// import java.security.NoSuchProviderException;
// import java.security.PrivateKey;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import com.example.auth_server.dilithium.DilithiumSignature;

// @Service
// public class DilithiumService {
// @Autowired
// DilithiumSignature dilithiumSignature;

// public KeyPair generateKeyPair()
// throws NoSuchAlgorithmException, NoSuchProviderException,
// InvalidAlgorithmParameterException {
// return this.dilithiumSignature.keyPair();
// }

// public String signData(String data, PrivateKey privateKey) {
// // Implementação da assinatura de dados usando a chave privada Dilithium
// return null; // Retornar a assinatura
// }

// public boolean verifySignature(String data, String signature, Key publicKey)
// {
// // Implementação da verificação da assinatura usando a chave pública
// Dilithium
// return false; // Retornar true se a assinatura for válida, caso contrário
// false
// }

// public DilithiumMetrics getMetrics() {
// // Implementação para obter métricas de desempenho
// return new DilithiumMetrics();
// }
// }
