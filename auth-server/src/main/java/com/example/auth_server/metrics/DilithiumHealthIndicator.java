package com.example.auth_server.metrics;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import com.example.auth_server.dilithium.DilithiumSignature;
import com.example.auth_server.dilithium.DilithiumKeyPoolService;
import com.example.auth_server.dilithium.DilithiumKeyPoolService.PoolStatistics;

import java.security.Security;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class DilithiumHealthIndicator implements HealthIndicator {

    @Autowired(required = false)
    private DilithiumSignature dilithiumSignature;

    @Autowired(required = false)
    private DilithiumKeyPoolService keyPoolService;

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();

        try {

            boolean bcProvider = Security.getProvider("BC") != null;
            boolean bcpqcProvider = Security.getProvider("BCPQC") != null;

            details.put("bouncyCastle", bcProvider ? "OK" : "NOT_LOADED");
            details.put("bouncyCastlePQC", bcpqcProvider ? "OK" : "NOT_LOADED");

            if (dilithiumSignature != null) {
                // Garantir que as chaves estão geradas
                if (dilithiumSignature.getPublicKey() == null) {
                    dilithiumSignature.keyPair();
                }

                // Teste funcional: assinar e verificar
                byte[] testData = "health_check_dilithium_tcc_unb".getBytes();

                long signStart = System.currentTimeMillis();
                byte[] signature = dilithiumSignature.sign(testData);
                long signDuration = System.currentTimeMillis() - signStart;

                long verifyStart = System.currentTimeMillis();
                boolean valid = dilithiumSignature.verify(testData, signature, dilithiumSignature.getPublicKey());
                long verifyDuration = System.currentTimeMillis() - verifyStart;

                details.put("dilithiumStatus", valid ? "OPERATIONAL" : "FAILED");
                details.put("dilithiumLevel", "3 (192-bit security)");
                details.put("keyPairGenerated", true);
                details.put("signatureTimeMs", signDuration);
                details.put("verificationTimeMs", verifyDuration);
                details.put("signatureSize", signature.length + " bytes");
            } else {
                details.put("dilithiumStatus", "NOT_CONFIGURED");
            }

            if (keyPoolService != null) {
                PoolStatistics stats = keyPoolService.getStatistics();

                Map<String, Object> poolInfo = new HashMap<>();
                poolInfo.put("currentSize", stats.getCurrentSize());
                poolInfo.put("maxSize", stats.getMaxSize());
                poolInfo.put("minSize", stats.getMinSize());
                poolInfo.put("utilizationPercent",
                        String.format("%.1f%%", (stats.getCurrentSize() * 100.0 / stats.getMaxSize())));
                poolInfo.put("lastRefillTime", stats.getLastRefillTime());

                details.put("keyPool", poolInfo);

                String poolStatus = "HEALTHY";
                if (stats.getCurrentSize() == 0) {
                    poolStatus = "EMPTY";
                } else if (stats.getCurrentSize() < stats.getMinSize()) {
                    poolStatus = "LOW";
                }
                details.put("keyPoolStatus", poolStatus);

                try {
                    long poolTestStart = System.currentTimeMillis();
                    var keyPair = keyPoolService.getKeyPairFromPool();
                    long poolTestDuration = System.currentTimeMillis() - poolTestStart;

                    if (keyPair != null) {
                        details.put("poolAccessTimeMs", poolTestDuration);
                        details.put("poolAccessStatus", "SUCCESS");
                    } else {
                        details.put("poolAccessStatus", "FAILED");
                    }
                } catch (Exception e) {
                    details.put("poolAccessStatus", "ERROR");
                    details.put("poolAccessError", e.getMessage());
                }
            } else {
                details.put("keyPoolStatus", "NOT_CONFIGURED");
            }

            Runtime runtime = Runtime.getRuntime();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();

            Map<String, Object> systemInfo = new HashMap<>();
            systemInfo.put("javaVersion", System.getProperty("java.version"));
            systemInfo.put("availableProcessors", runtime.availableProcessors());
            systemInfo.put("freeMemoryMB", runtime.freeMemory() / (1024 * 1024));
            systemInfo.put("totalMemoryMB", runtime.totalMemory() / (1024 * 1024));
            systemInfo.put("usedMemoryMB", usedMemory / (1024 * 1024));
            systemInfo.put("memoryUtilizationPercent",
                    String.format("%.1f%%", (usedMemory * 100.0 / runtime.totalMemory())));

            details.put("system", systemInfo);
            details.put("timestamp", LocalDateTime.now().toString());
            details.put("environment", "docker");

            return Health.up()
                    .withDetails(details)
                    .build();

        } catch (Exception e) {
            return Health.down()
                    .withException(e)
                    .withDetail("error", e.getMessage())
                    .withDetail("errorType", e.getClass().getName())
                    .withDetail("stackTrace", getFirstLines(e.getStackTrace(), 5))
                    .build();
        }
    }

    private String getFirstLines(StackTraceElement[] stackTrace, int lines) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(lines, stackTrace.length); i++) {
            sb.append(stackTrace[i].toString()).append("\n");
        }
        return sb.toString();
    }
}