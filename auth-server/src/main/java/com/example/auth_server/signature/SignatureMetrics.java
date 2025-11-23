package com.example.auth_server.signature;

public class SignatureMetrics {
    private long keyGenerationTime;
    private long signatureTime;
    private long verificationTime;
    private int keySize;
    private String algorithm;
    
    public SignatureMetrics() {}
    
    public long getKeyGenerationTime() { return keyGenerationTime; }
    public void setKeyGenerationTime(long keyGenerationTime) { this.keyGenerationTime = keyGenerationTime; }
    
    public long getSignatureTime() { return signatureTime; }
    public void setSignatureTime(long signatureTime) { this.signatureTime = signatureTime; }
    
    public long getVerificationTime() { return verificationTime; }
    public void setVerificationTime(long verificationTime) { this.verificationTime = verificationTime; }
    
    public int getKeySize() { return keySize; }
    public void setKeySize(int keySize) { this.keySize = keySize; }
    
    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
}
