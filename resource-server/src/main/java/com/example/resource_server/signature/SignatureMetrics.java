package com.example.resource_server.signature;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class SignatureMetrics {

    private long keyGenerationTime;
    private List<Long> signTimes = new ArrayList<>();
    private List<Long> verifyTimes = new ArrayList<>();
    private int totalSignOperations = 0;
    private int totalVerifyOperations = 0;

    public void recordSignOperation(long durationMs) {
        signTimes.add(durationMs);
        totalSignOperations++;
    }

    public void recordVerifyOperation(long durationMs) {
        verifyTimes.add(durationMs);
        totalVerifyOperations++;
    }

    public double getAverageSignTime() {
        if (signTimes.isEmpty())
            return 0;
        return signTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);
    }

    public double getAverageVerifyTime() {
        if (verifyTimes.isEmpty())
            return 0;
        return verifyTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);
    }
}