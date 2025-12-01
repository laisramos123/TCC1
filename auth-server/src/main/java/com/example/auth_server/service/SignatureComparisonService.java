package com.example.auth_server.service;

import com.example.auth_server.metrics.SignatureMetricsExporter;
import com.example.auth_server.signature.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class SignatureComparisonService {

    private final SignatureAlgorithm rsaAlgorithm;
    private final SignatureAlgorithm dilithiumAlgorithm;
    private final SignatureMetricsExporter metricsExporter;

    public SignatureComparisonService(
            @Qualifier("rsaSignature") SignatureAlgorithm rsaAlgorithm,
            @Qualifier("dilithiumAlgorithm") SignatureAlgorithm dilithiumAlgorithm,
            SignatureMetricsExporter metricsExporter) {
        this.rsaAlgorithm = rsaAlgorithm;
        this.dilithiumAlgorithm = dilithiumAlgorithm;
        this.metricsExporter = metricsExporter;
    }

    public Map<String, Object> compareSignature(String data) throws Exception {
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        Map<String, Object> result = new HashMap<>();

        Map<String, Object> rsaResult = new HashMap<>();

        if (rsaAlgorithm.getPublicKey() == null) {
            long startKeyGen = System.nanoTime();
            rsaAlgorithm.generateKeyPair();
            long keyGenDuration = System.nanoTime() - startKeyGen;
            metricsExporter.recordRsaKeyGeneration(
                    keyGenDuration / 1_000_000,
                    rsaAlgorithm.getPublicKey().getEncoded().length);
        }

        long rsaSignStart = System.nanoTime();
        byte[] rsaSignature = rsaAlgorithm.sign(dataBytes);
        long rsaSignDuration = System.nanoTime() - rsaSignStart;
        metricsExporter.recordRsaSign(rsaSignDuration, rsaSignature.length);

        long rsaVerifyStart = System.nanoTime();
        boolean rsaValid = rsaAlgorithm.verify(dataBytes, rsaSignature, rsaAlgorithm.getPublicKey());
        long rsaVerifyDuration = System.nanoTime() - rsaVerifyStart;
        metricsExporter.recordRsaVerify(rsaVerifyDuration);

        rsaResult.put("algorithm", "RSA-2048 (RS256)");
        rsaResult.put("signTimeMs", rsaSignDuration / 1_000_000.0);
        rsaResult.put("verifyTimeMs", rsaVerifyDuration / 1_000_000.0);
        rsaResult.put("signatureSize", rsaSignature.length);
        rsaResult.put("publicKeySize", rsaAlgorithm.getPublicKey().getEncoded().length);
        rsaResult.put("valid", rsaValid);
        rsaResult.put("quantumResistant", false);

        Map<String, Object> dilithiumResult = new HashMap<>();

        if (dilithiumAlgorithm.getPublicKey() == null) {
            long startKeyGen = System.nanoTime();
            dilithiumAlgorithm.generateKeyPair();
            long keyGenDuration = System.nanoTime() - startKeyGen;
            metricsExporter.recordDilithiumKeyGeneration(
                    keyGenDuration / 1_000_000,
                    dilithiumAlgorithm.getPublicKey().getEncoded().length);
        }

        long dilithiumSignStart = System.nanoTime();
        byte[] dilithiumSignature = dilithiumAlgorithm.sign(dataBytes);
        long dilithiumSignDuration = System.nanoTime() - dilithiumSignStart;
        metricsExporter.recordDilithiumSign(dilithiumSignDuration, dilithiumSignature.length);

        long dilithiumVerifyStart = System.nanoTime();
        boolean dilithiumValid = dilithiumAlgorithm.verify(
                dataBytes, dilithiumSignature, dilithiumAlgorithm.getPublicKey());
        long dilithiumVerifyDuration = System.nanoTime() - dilithiumVerifyStart;
        metricsExporter.recordDilithiumVerify(dilithiumVerifyDuration);

        dilithiumResult.put("algorithm", "Dilithium3 (NIST PQC)");
        dilithiumResult.put("signTimeMs", dilithiumSignDuration / 1_000_000.0);
        dilithiumResult.put("verifyTimeMs", dilithiumVerifyDuration / 1_000_000.0);
        dilithiumResult.put("signatureSize", dilithiumSignature.length);
        dilithiumResult.put("publicKeySize", dilithiumAlgorithm.getPublicKey().getEncoded().length);
        dilithiumResult.put("valid", dilithiumValid);
        dilithiumResult.put("quantumResistant", true);

        Map<String, Object> comparison = new HashMap<>();
        comparison.put("signSpeedRatio",
                (double) rsaSignDuration / dilithiumSignDuration);
        comparison.put("verifySpeedRatio",
                (double) rsaVerifyDuration / dilithiumVerifyDuration);
        comparison.put("signatureSizeRatio",
                (double) dilithiumSignature.length / rsaSignature.length);
        comparison.put("publicKeySizeRatio",
                (double) dilithiumAlgorithm.getPublicKey().getEncoded().length /
                        rsaAlgorithm.getPublicKey().getEncoded().length);

        result.put("rsa", rsaResult);
        result.put("dilithium", dilithiumResult);
        result.put("comparison", comparison);
        result.put("dataSize", dataBytes.length);
        result.put("timestamp", System.currentTimeMillis());

        log.info("  Comparação RSA vs Dilithium:");
        log.info("   RSA Sign: {:.3f}ms | Dilithium Sign: {:.3f}ms",
                rsaSignDuration / 1_000_000.0, dilithiumSignDuration / 1_000_000.0);
        log.info("   RSA Verify: {:.3f}ms | Dilithium Verify: {:.3f}ms",
                rsaVerifyDuration / 1_000_000.0, dilithiumVerifyDuration / 1_000_000.0);

        return result;
    }

    public Map<String, Object> runBenchmark(String data, int iterations) throws Exception {
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);

        if (rsaAlgorithm.getPublicKey() == null)
            rsaAlgorithm.generateKeyPair();
        if (dilithiumAlgorithm.getPublicKey() == null)
            dilithiumAlgorithm.generateKeyPair();

        long[] rsaSignTimes = new long[iterations];
        long[] rsaVerifyTimes = new long[iterations];
        long[] dilithiumSignTimes = new long[iterations];
        long[] dilithiumVerifyTimes = new long[iterations];

        for (int i = 0; i < 5; i++) {
            rsaAlgorithm.sign(dataBytes);
            dilithiumAlgorithm.sign(dataBytes);
        }

        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            byte[] sig = rsaAlgorithm.sign(dataBytes);
            rsaSignTimes[i] = System.nanoTime() - start;

            start = System.nanoTime();
            rsaAlgorithm.verify(dataBytes, sig, rsaAlgorithm.getPublicKey());
            rsaVerifyTimes[i] = System.nanoTime() - start;
        }

        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            byte[] sig = dilithiumAlgorithm.sign(dataBytes);
            dilithiumSignTimes[i] = System.nanoTime() - start;

            start = System.nanoTime();
            dilithiumAlgorithm.verify(dataBytes, sig, dilithiumAlgorithm.getPublicKey());
            dilithiumVerifyTimes[i] = System.nanoTime() - start;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("iterations", iterations);
        result.put("dataSize", dataBytes.length);

        result.put("rsa", Map.of(
                "avgSignMs", average(rsaSignTimes) / 1_000_000.0,
                "avgVerifyMs", average(rsaVerifyTimes) / 1_000_000.0,
                "minSignMs", min(rsaSignTimes) / 1_000_000.0,
                "maxSignMs", max(rsaSignTimes) / 1_000_000.0,
                "stdDevSignMs", stdDev(rsaSignTimes) / 1_000_000.0));

        result.put("dilithium", Map.of(
                "avgSignMs", average(dilithiumSignTimes) / 1_000_000.0,
                "avgVerifyMs", average(dilithiumVerifyTimes) / 1_000_000.0,
                "minSignMs", min(dilithiumSignTimes) / 1_000_000.0,
                "maxSignMs", max(dilithiumSignTimes) / 1_000_000.0,
                "stdDevSignMs", stdDev(dilithiumSignTimes) / 1_000_000.0));

        return result;
    }

    private double average(long[] values) {
        long sum = 0;
        for (long v : values)
            sum += v;
        return (double) sum / values.length;
    }

    private long min(long[] values) {
        long min = Long.MAX_VALUE;
        for (long v : values)
            if (v < min)
                min = v;
        return min;
    }

    private long max(long[] values) {
        long max = Long.MIN_VALUE;
        for (long v : values)
            if (v > max)
                max = v;
        return max;
    }

    private double stdDev(long[] values) {
        double avg = average(values);
        double sumSquares = 0;
        for (long v : values)
            sumSquares += Math.pow(v - avg, 2);
        return Math.sqrt(sumSquares / values.length);
    }
}