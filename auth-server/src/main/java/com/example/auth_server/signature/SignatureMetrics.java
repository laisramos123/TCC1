package com.example.auth_server.signature;

import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class SignatureMetrics {
    private final AtomicLong keyGenerationTime = new AtomicLong(0);
    private final AtomicLong signOperations = new AtomicLong(0);
    private final AtomicLong verifyOperations = new AtomicLong(0);
    private final AtomicLong totalSignTime = new AtomicLong(0);
    private final AtomicLong totalVerifyTime = new AtomicLong(0);
    
    public void setKeyGenerationTime(long time) {
        keyGenerationTime.set(time);
    }
    
    public long getKeyGenerationTime() {
        return keyGenerationTime.get();
    }
    
    public void recordSignOperation(long duration) {
        signOperations.incrementAndGet();
        totalSignTime.addAndGet(duration);
    }
    
    public void recordVerifyOperation(long duration) {
        verifyOperations.incrementAndGet();
        totalVerifyTime.addAndGet(duration);
    }
    
    public long getSignOperations() {
        return signOperations.get();
    }
    
    public long getVerifyOperations() {
        return verifyOperations.get();
    }
    
    public double getAverageSignTime() {
        long ops = signOperations.get();
        return ops > 0 ? (double) totalSignTime.get() / ops : 0;
    }
    
    public double getAverageVerifyTime() {
        long ops = verifyOperations.get();
        return ops > 0 ? (double) totalVerifyTime.get() / ops : 0;
    }
}
