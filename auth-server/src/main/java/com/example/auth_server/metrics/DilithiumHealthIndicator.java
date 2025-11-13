package com.example.auth_server.metrics;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import com.example.auth_server.dilithium.DilithiumSignature;
import com.example.auth_server.repository.ConsentRepository;

import java.security.Security;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class DilithiumHealthIndicator implements HealthIndicator {

    @Autowired(required = false)
    private DilithiumSignature dilithiumSignature;

    @Autowired
    private ConsentRepository consentRepository;

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();

        try {

            boolean bcProvider = Security.getProvider("BC") != null;
            boolean bcpqcProvider = Security.getProvider("BCPQC") != null;

            details.put("bouncyCastle", bcProvider ? "OK" : "NOT_LOADED");
            details.put("bouncyCastlePQC", bcpqcProvider ? "OK" : "NOT_LOADED");

            if (dilithiumSignature != null) {
                if (dilithiumSignature.getPublicKey() == null) {
                    dilithiumSignature.keyPair();
                }

                byte[] testData = "health_check".getBytes();
                byte[] signature = dilithiumSignature.sign(testData);
                boolean valid = dilithiumSignature.verify(testData, signature, dilithiumSignature.getPublicKey());

                details.put("dilithiumStatus", valid ? "OPERATIONAL" : "FAILED");
                details.put("dilithiumLevel", "3 (192-bit security)");
                details.put("keyPairGenerated", dilithiumSignature.getPublicKey() != null);
            } else {
                details.put("dilithiumStatus", "NOT_CONFIGURED");
            }

            long consentCount = consentRepository.count();
            details.put("database", "CONNECTED");
            details.put("totalConsents", consentCount);

            details.put("timestamp", LocalDateTime.now());
            details.put("javaVersion", System.getProperty("java.version"));
            details.put("availableProcessors", Runtime.getRuntime().availableProcessors());
            details.put("freeMemoryMB", Runtime.getRuntime().freeMemory() / (1024 * 1024));
            details.put("totalMemoryMB", Runtime.getRuntime().totalMemory() / (1024 * 1024));

            return Health.up()
                    .withDetails(details)
                    .build();

        } catch (Exception e) {
            return Health.down()
                    .withException(e)
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
