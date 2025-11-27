package com.example.auth_server.controller;

import com.example.auth_server.signature.SignatureAlgorithm;
import com.example.auth_server.metrics.DilithiumMetrics;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;

@RestController
@RequestMapping("/api/v1/benchmark")
public class BenchmarkController {

    private final SignatureAlgorithm rsaSignature;
    private final SignatureAlgorithm dilithiumSignature;
    private final DilithiumMetrics dilithiumMetrics;

    private final OperatingSystemMXBean osMXBean;
    private final ThreadMXBean threadMXBean;
    private final com.sun.management.OperatingSystemMXBean sunOsMXBean;

    public BenchmarkController(
            @Qualifier("rsaSignature") SignatureAlgorithm rsaSignature,
            @Qualifier("dilithiumAlgorithm") SignatureAlgorithm dilithiumSignature,
            DilithiumMetrics dilithiumMetrics) {
        this.rsaSignature = rsaSignature;
        this.dilithiumSignature = dilithiumSignature;
        this.dilithiumMetrics = dilithiumMetrics;

        this.osMXBean = ManagementFactory.getOperatingSystemMXBean();
        this.threadMXBean = ManagementFactory.getThreadMXBean();

        if (osMXBean instanceof com.sun.management.OperatingSystemMXBean) {
            this.sunOsMXBean = (com.sun.management.OperatingSystemMXBean) osMXBean;
        } else {
            this.sunOsMXBean = null;
        }
    }

    @GetMapping("/compare")
    public ResponseEntity<Map<String, Object>> runComparison(
            @RequestParam(defaultValue = "100") int iterations,
            @RequestParam(defaultValue = "256") int payloadSize) throws Exception {

        if (iterations < 1 || iterations > 10000) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "iterations deve estar entre 1 e 10000"));
        }
        if (payloadSize < 1 || payloadSize > 10000) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "payloadSize deve estar entre 1 e 10000 bytes"));
        }

        byte[] testData = generateTestPayload(payloadSize);

        if (rsaSignature.getPublicKey() == null) {
            rsaSignature.generateKeyPair();
        }
        if (dilithiumSignature.getPublicKey() == null) {
            dilithiumSignature.generateKeyPair();
        }

        warmup(testData);

        System.gc();
        Thread.sleep(100);

        Map<String, Object> rsaResults = benchmarkAlgorithmWithCpu(rsaSignature, testData, iterations);

        System.gc();
        Thread.sleep(100);

        Map<String, Object> dilithiumResults = benchmarkAlgorithmWithCpu(dilithiumSignature, testData, iterations);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("testParameters", Map.of(
                "iterations", iterations,
                "payloadSizeBytes", payloadSize,
                "timestamp", System.currentTimeMillis(),
                "javaVersion", System.getProperty("java.version"),
                "osName", System.getProperty("os.name"),
                "availableProcessors", Runtime.getRuntime().availableProcessors()));
        response.put("rsa", rsaResults);
        response.put("dilithium", dilithiumResults);
        response.put("comparison", calculateComparison(rsaResults, dilithiumResults));
        response.put("securityAnalysis", getSecurityAnalysis());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/rsa")
    public ResponseEntity<Map<String, Object>> benchmarkRsa(
            @RequestParam(defaultValue = "100") int iterations,
            @RequestParam(defaultValue = "256") int payloadSize) throws Exception {

        byte[] testData = generateTestPayload(payloadSize);

        if (rsaSignature.getPublicKey() == null) {
            rsaSignature.generateKeyPair();
        }

        for (int i = 0; i < 10; i++) {
            rsaSignature.sign(testData);
        }

        System.gc();
        Thread.sleep(50);

        Map<String, Object> results = benchmarkAlgorithmWithCpu(rsaSignature, testData, iterations);
        results.put("testParameters", Map.of(
                "iterations", iterations,
                "payloadSizeBytes", payloadSize));

        return ResponseEntity.ok(results);
    }

    @GetMapping("/dilithium")
    public ResponseEntity<Map<String, Object>> benchmarkDilithium(
            @RequestParam(defaultValue = "100") int iterations,
            @RequestParam(defaultValue = "256") int payloadSize) throws Exception {

        byte[] testData = generateTestPayload(payloadSize);

        if (dilithiumSignature.getPublicKey() == null) {
            dilithiumSignature.generateKeyPair();
        }

        for (int i = 0; i < 10; i++) {
            dilithiumSignature.sign(testData);
        }

        System.gc();
        Thread.sleep(50);

        Map<String, Object> results = benchmarkAlgorithmWithCpu(dilithiumSignature, testData, iterations);
        results.put("testParameters", Map.of(
                "iterations", iterations,
                "payloadSizeBytes", payloadSize));

        @SuppressWarnings("unchecked")
        Map<String, Object> signing = (Map<String, Object>) results.get("signing");
        long avgSignTime = ((Double) signing.get("avgMicroseconds")).longValue() / 1000;
        dilithiumMetrics.recordSignature(avgSignTime);

        return ResponseEntity.ok(results);
    }

    @GetMapping("/keygen")
    public ResponseEntity<Map<String, Object>> benchmarkKeyGeneration(
            @RequestParam(defaultValue = "10") int iterations) throws Exception {

        if (iterations < 1 || iterations > 100) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "iterations deve estar entre 1 e 100 para geração de chaves"));
        }

        Map<String, Object> response = new LinkedHashMap<>();

        System.gc();
        Thread.sleep(50);

        double rsaCpuBefore = getProcessCpuLoad();
        long rsaCpuTimeBefore = getCurrentThreadCpuTime();
        List<Long> rsaKeyGenTimes = new ArrayList<>();
        List<Double> rsaCpuSamples = new ArrayList<>();

        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            rsaSignature.generateKeyPair();
            long duration = (System.nanoTime() - start) / 1_000_000; // ms
            rsaKeyGenTimes.add(duration);
            rsaCpuSamples.add(getProcessCpuLoad());
        }

        long rsaCpuTimeAfter = getCurrentThreadCpuTime();
        double rsaCpuAfter = getProcessCpuLoad();

        System.gc();
        Thread.sleep(50);

        double dilCpuBefore = getProcessCpuLoad();
        long dilCpuTimeBefore = getCurrentThreadCpuTime();
        List<Long> dilithiumKeyGenTimes = new ArrayList<>();
        List<Double> dilCpuSamples = new ArrayList<>();

        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            dilithiumSignature.generateKeyPair();
            long duration = (System.nanoTime() - start) / 1_000_000; // ms
            dilithiumKeyGenTimes.add(duration);
            dilCpuSamples.add(getProcessCpuLoad());

            dilithiumMetrics.recordKeyGeneration(duration);
        }

        long dilCpuTimeAfter = getCurrentThreadCpuTime();
        double dilCpuAfter = getProcessCpuLoad();

        response.put("iterations", iterations);
        response.put("rsa", Map.of(
                "algorithm", "RSA-2048",
                "avgMilliseconds", calculateAverageLong(rsaKeyGenTimes),
                "minMilliseconds", Collections.min(rsaKeyGenTimes),
                "maxMilliseconds", Collections.max(rsaKeyGenTimes),
                "publicKeySizeBytes", rsaSignature.getPublicKey().getEncoded().length,
                "privateKeySizeBytes", rsaSignature.getPrivateKey().getEncoded().length,
                "cpu", Map.of(
                        "beforePercent", formatPercent(rsaCpuBefore),
                        "afterPercent", formatPercent(rsaCpuAfter),
                        "avgPercent", formatPercent(calculateAverageDouble(rsaCpuSamples)),
                        "peakPercent", formatPercent(Collections.max(rsaCpuSamples)),
                        "threadCpuTimeMs", (rsaCpuTimeAfter - rsaCpuTimeBefore) / 1_000_000)));

        response.put("dilithium", Map.of(
                "algorithm", "Dilithium3 (NIST Level 3)",
                "avgMilliseconds", calculateAverageLong(dilithiumKeyGenTimes),
                "minMilliseconds", Collections.min(dilithiumKeyGenTimes),
                "maxMilliseconds", Collections.max(dilithiumKeyGenTimes),
                "publicKeySizeBytes", dilithiumSignature.getPublicKey().getEncoded().length,
                "privateKeySizeBytes", dilithiumSignature.getPrivateKey().getEncoded().length,
                "cpu", Map.of(
                        "beforePercent", formatPercent(dilCpuBefore),
                        "afterPercent", formatPercent(dilCpuAfter),
                        "avgPercent", formatPercent(calculateAverageDouble(dilCpuSamples)),
                        "peakPercent", formatPercent(Collections.max(dilCpuSamples)),
                        "threadCpuTimeMs", (dilCpuTimeAfter - dilCpuTimeBefore) / 1_000_000)));

        double rsaAvg = calculateAverageLong(rsaKeyGenTimes);
        double dilAvg = calculateAverageLong(dilithiumKeyGenTimes);
        double rsaCpuAvg = calculateAverageDouble(rsaCpuSamples);
        double dilCpuAvg = calculateAverageDouble(dilCpuSamples);

        response.put("comparison", Map.of(
                "speedRatio", String.format("%.2fx", dilAvg / rsaAvg),
                "fasterAlgorithm", rsaAvg < dilAvg ? "RSA" : "Dilithium",
                "cpuEfficiency", Map.of(
                        "rsaAvgCpuPercent", formatPercent(rsaCpuAvg),
                        "dilithiumAvgCpuPercent", formatPercent(dilCpuAvg),
                        "lowerCpuUsage", rsaCpuAvg < dilCpuAvg ? "RSA" : "Dilithium")));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/jwt-simulation")
    public ResponseEntity<Map<String, Object>> simulateJwtSigning(
            @RequestParam(defaultValue = "50") int iterations) throws Exception {

        String jwtPayload = String.format("""
                {
                    "iss": "http://localhost:8080",
                    "sub": "12345678900",
                    "aud": "open-finance-client",
                    "exp": %d,
                    "iat": %d,
                    "consent_id": "urn:bancoexemplo:C1DD33123",
                    "scope": "openid accounts credit-cards-accounts",
                    "client_id": "oauth-client",
                    "organization_id": "banco-organizacao-123",
                    "software_id": "tpp-software-456"
                }
                """,
                System.currentTimeMillis() / 1000 + 3600,
                System.currentTimeMillis() / 1000);

        byte[] payloadBytes = jwtPayload.getBytes(StandardCharsets.UTF_8);

        if (rsaSignature.getPublicKey() == null)
            rsaSignature.generateKeyPair();
        if (dilithiumSignature.getPublicKey() == null)
            dilithiumSignature.generateKeyPair();

        warmup(payloadBytes);

        System.gc();
        Thread.sleep(100);

        Map<String, Object> rsaResults = benchmarkAlgorithmWithCpu(rsaSignature, payloadBytes, iterations);

        System.gc();
        Thread.sleep(100);

        Map<String, Object> dilithiumResults = benchmarkAlgorithmWithCpu(dilithiumSignature, payloadBytes, iterations);

        int rsaJwtSize = calculateApproximateJwtSize(payloadBytes.length, (Integer) rsaResults.get("signatureSize"));
        int dilithiumJwtSize = calculateApproximateJwtSize(payloadBytes.length,
                (Integer) dilithiumResults.get("signatureSize"));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("scenario", "OAuth2 JWT Token - Open Finance Brasil");
        response.put("payloadSizeBytes", payloadBytes.length);
        response.put("iterations", iterations);

        rsaResults.put("estimatedJwtSizeBytes", rsaJwtSize);
        dilithiumResults.put("estimatedJwtSizeBytes", dilithiumJwtSize);

        response.put("rsa", rsaResults);
        response.put("dilithium", dilithiumResults);
        response.put("comparison", calculateComparison(rsaResults, dilithiumResults));

        @SuppressWarnings("unchecked")
        Map<String, Object> rsaCpu = (Map<String, Object>) rsaResults.get("cpu");
        @SuppressWarnings("unchecked")
        Map<String, Object> dilCpu = (Map<String, Object>) dilithiumResults.get("cpu");

        response.put("openFinanceImpact", Map.of(
                "jwtSizeIncrease", String.format("%.1fx maior com Dilithium", (double) dilithiumJwtSize / rsaJwtSize),
                "networkOverheadPerRequest", (dilithiumJwtSize - rsaJwtSize) + " bytes adicionais",
                "cpuComparison", Map.of(
                        "rsaAvgCpu", rsaCpu.get("avgPercent"),
                        "dilithiumAvgCpu", dilCpu.get("avgPercent"),
                        "moreEfficient", compareCpuEfficiency(rsaCpu, dilCpu)),
                "quantumSecurityBenefit", "Proteção contra ataques de computadores quânticos (Shor's algorithm)"));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/cpu")
    public ResponseEntity<Map<String, Object>> getCpuMetrics() {
        Map<String, Object> cpuMetrics = new LinkedHashMap<>();

        cpuMetrics.put("timestamp", System.currentTimeMillis());
        cpuMetrics.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        cpuMetrics.put("systemLoadAverage", osMXBean.getSystemLoadAverage());

        if (sunOsMXBean != null) {
            cpuMetrics.put("processCpuLoad", formatPercent(sunOsMXBean.getProcessCpuLoad()));
            cpuMetrics.put("systemCpuLoad", formatPercent(sunOsMXBean.getCpuLoad()));
            cpuMetrics.put("processCpuTimeNs", sunOsMXBean.getProcessCpuTime());
        }

        Runtime runtime = Runtime.getRuntime();
        cpuMetrics.put("memory", Map.of(
                "totalMB", runtime.totalMemory() / (1024 * 1024),
                "freeMB", runtime.freeMemory() / (1024 * 1024),
                "usedMB", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024),
                "maxMB", runtime.maxMemory() / (1024 * 1024)));

        cpuMetrics.put("threads", Map.of(
                "activeCount", Thread.activeCount(),
                "threadCount", threadMXBean.getThreadCount(),
                "peakThreadCount", threadMXBean.getPeakThreadCount(),
                "totalStartedThreadCount", threadMXBean.getTotalStartedThreadCount()));

        return ResponseEntity.ok(cpuMetrics);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAccumulatedStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        stats.put("rsa", Map.of(
                "algorithm", rsaSignature.getAlgorithmName(),
                "keyGenerationTimeMs", rsaSignature.getMetrics().getKeyGenerationTime(),
                "totalSignOperations", rsaSignature.getMetrics().getSignOperations(),
                "totalVerifyOperations", rsaSignature.getMetrics().getVerifyOperations(),
                "avgSignTimeMs", rsaSignature.getMetrics().getAverageSignTime(),
                "avgVerifyTimeMs", rsaSignature.getMetrics().getAverageVerifyTime()));

        stats.put("dilithium", Map.of(
                "algorithm", dilithiumSignature.getAlgorithmName(),
                "keyGenerationTimeMs", dilithiumSignature.getMetrics().getKeyGenerationTime(),
                "totalSignOperations", dilithiumSignature.getMetrics().getSignOperations(),
                "totalVerifyOperations", dilithiumSignature.getMetrics().getVerifyOperations(),
                "avgSignTimeMs", dilithiumSignature.getMetrics().getAverageSignTime(),
                "avgVerifyTimeMs", dilithiumSignature.getMetrics().getAverageVerifyTime()));

        stats.put("currentCpu", Map.of(
                "processCpuPercent", formatPercent(getProcessCpuLoad()),
                "availableProcessors", Runtime.getRuntime().availableProcessors()));

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getAlgorithmInfo() {
        Map<String, Object> info = new LinkedHashMap<>();

        info.put("rsa", Map.of(
                "name", "RSA-2048 with SHA-256",
                "jwtAlgorithm", "RS256",
                "securityLevel", "112 bits (classical)",
                "quantumResistant", false,
                "standardization", "PKCS#1, RFC 8017",
                "keySize", "2048 bits",
                "signatureSize", "256 bytes",
                "useCase", "Assinatura digital tradicional, amplamente suportada"));

        info.put("dilithium", Map.of(
                "name", "CRYSTALS-Dilithium (Level 3)",
                "jwtAlgorithm", "DILITHIUM3",
                "securityLevel", "192 bits (post-quantum)",
                "quantumResistant", true,
                "standardization", "NIST FIPS 204 (ML-DSA)",
                "keySize", "Public: 1952 bytes, Private: 4000 bytes",
                "signatureSize", "3293 bytes",
                "useCase", "Assinatura digital pós-quântica para proteção futura"));

        info.put("recommendation", Map.of(
                "shortTerm", "RSA-2048 para compatibilidade",
                "longTerm", "Migração para Dilithium antes de 2030",
                "reason", "Computadores quânticos podem quebrar RSA em 10-15 anos"));

        return ResponseEntity.ok(info);
    }

    private double getProcessCpuLoad() {
        if (sunOsMXBean != null) {
            double load = sunOsMXBean.getProcessCpuLoad();
            return load < 0 ? 0 : load;
        }
        return -1;
    }

    private long getCurrentThreadCpuTime() {
        if (threadMXBean.isCurrentThreadCpuTimeSupported()) {
            return threadMXBean.getCurrentThreadCpuTime();
        }
        return 0;
    }

    private double formatPercent(double value) {
        if (value < 0)
            return 0;
        return Math.round(value * 10000.0) / 100.0;
    }

    private String compareCpuEfficiency(Map<String, Object> rsaCpu, Map<String, Object> dilCpu) {
        double rsaAvg = (Double) rsaCpu.get("avgPercent");
        double dilAvg = (Double) dilCpu.get("avgPercent");

        if (Math.abs(rsaAvg - dilAvg) < 1.0) {
            return "Similar";
        }
        return rsaAvg < dilAvg ? "RSA (menor uso de CPU)" : "Dilithium (menor uso de CPU)";
    }

    private void warmup(byte[] testData) throws Exception {
        for (int i = 0; i < 10; i++) {
            byte[] rsaSig = rsaSignature.sign(testData);
            rsaSignature.verify(testData, rsaSig, rsaSignature.getPublicKey());

            byte[] dilSig = dilithiumSignature.sign(testData);
            dilithiumSignature.verify(testData, dilSig, dilithiumSignature.getPublicKey());
        }
    }

    private Map<String, Object> benchmarkAlgorithmWithCpu(
            SignatureAlgorithm algorithm,
            byte[] data,
            int iterations) throws Exception {

        List<Long> signTimes = new ArrayList<>();
        List<Long> verifyTimes = new ArrayList<>();
        List<Double> cpuSamples = new ArrayList<>();
        byte[] lastSignature = null;

        // Métricas de CPU antes do benchmark
        double cpuBefore = getProcessCpuLoad();
        long threadCpuBefore = getCurrentThreadCpuTime();
        double peakCpu = cpuBefore;

        for (int i = 0; i < iterations; i++) {

            long signStart = System.nanoTime();
            lastSignature = algorithm.sign(data);
            long signEnd = System.nanoTime();
            signTimes.add((signEnd - signStart) / 1000);

            long verifyStart = System.nanoTime();
            algorithm.verify(data, lastSignature, algorithm.getPublicKey());
            long verifyEnd = System.nanoTime();
            verifyTimes.add((verifyEnd - verifyStart) / 1000);

            if (i % 10 == 0) {
                double currentCpu = getProcessCpuLoad();
                cpuSamples.add(currentCpu);
                if (currentCpu > peakCpu) {
                    peakCpu = currentCpu;
                }
            }
        }

        double cpuAfter = getProcessCpuLoad();
        long threadCpuAfter = getCurrentThreadCpuTime();

        Map<String, Object> results = new LinkedHashMap<>();
        results.put("algorithm", algorithm.getAlgorithmName());
        results.put("jwtHeader", algorithm.getJwtAlgorithmHeader());
        results.put("signatureSize", lastSignature != null ? lastSignature.length : 0);
        results.put("publicKeySize", algorithm.getPublicKey().getEncoded().length);

        results.put("signing", Map.of(
                "avgMicroseconds", calculateAverage(signTimes),
                "minMicroseconds", Collections.min(signTimes),
                "maxMicroseconds", Collections.max(signTimes),
                "p50Microseconds", percentile(signTimes, 50),
                "p95Microseconds", percentile(signTimes, 95),
                "p99Microseconds", percentile(signTimes, 99),
                "throughputPerSecond", calculateThroughput(signTimes)));

        results.put("verification", Map.of(
                "avgMicroseconds", calculateAverage(verifyTimes),
                "minMicroseconds", Collections.min(verifyTimes),
                "maxMicroseconds", Collections.max(verifyTimes),
                "p50Microseconds", percentile(verifyTimes, 50),
                "p95Microseconds", percentile(verifyTimes, 95),
                "p99Microseconds", percentile(verifyTimes, 99),
                "throughputPerSecond", calculateThroughput(verifyTimes)));

        results.put("cpu", Map.of(
                "beforePercent", formatPercent(cpuBefore),
                "afterPercent", formatPercent(cpuAfter),
                "avgPercent", formatPercent(cpuSamples.isEmpty() ? 0 : calculateAverageDouble(cpuSamples)),
                "peakPercent", formatPercent(peakCpu),
                "threadCpuTimeMs", (threadCpuAfter - threadCpuBefore) / 1_000_000,
                "samplesCollected", cpuSamples.size()));

        return results;
    }

    private Map<String, Object> calculateComparison(
            Map<String, Object> rsa,
            Map<String, Object> dilithium) {

        @SuppressWarnings("unchecked")
        Map<String, Object> rsaSign = (Map<String, Object>) rsa.get("signing");
        @SuppressWarnings("unchecked")
        Map<String, Object> dilSign = (Map<String, Object>) dilithium.get("signing");
        @SuppressWarnings("unchecked")
        Map<String, Object> rsaVerify = (Map<String, Object>) rsa.get("verification");
        @SuppressWarnings("unchecked")
        Map<String, Object> dilVerify = (Map<String, Object>) dilithium.get("verification");
        @SuppressWarnings("unchecked")
        Map<String, Object> rsaCpu = (Map<String, Object>) rsa.get("cpu");
        @SuppressWarnings("unchecked")
        Map<String, Object> dilCpu = (Map<String, Object>) dilithium.get("cpu");

        double rsaSignAvg = (Double) rsaSign.get("avgMicroseconds");
        double dilSignAvg = (Double) dilSign.get("avgMicroseconds");
        double rsaVerifyAvg = (Double) rsaVerify.get("avgMicroseconds");
        double dilVerifyAvg = (Double) dilVerify.get("avgMicroseconds");

        int rsaSigSize = (Integer) rsa.get("signatureSize");
        int dilSigSize = (Integer) dilithium.get("signatureSize");
        int rsaKeySize = (Integer) rsa.get("publicKeySize");
        int dilKeySize = (Integer) dilithium.get("publicKeySize");

        double rsaCpuAvg = (Double) rsaCpu.get("avgPercent");
        double dilCpuAvg = (Double) dilCpu.get("avgPercent");

        Map<String, Object> comparison = new LinkedHashMap<>();

        comparison.put("signingSpeed", Map.of(
                "ratio", String.format("%.2fx", dilSignAvg / rsaSignAvg),
                "faster", rsaSignAvg < dilSignAvg ? "RSA" : "Dilithium",
                "differencePercent", String.format("%.1f%%", Math.abs(dilSignAvg - rsaSignAvg) / rsaSignAvg * 100)));

        comparison.put("verificationSpeed", Map.of(
                "ratio", String.format("%.2fx", dilVerifyAvg / rsaVerifyAvg),
                "faster", rsaVerifyAvg < dilVerifyAvg ? "RSA" : "Dilithium",
                "differencePercent",
                String.format("%.1f%%", Math.abs(dilVerifyAvg - rsaVerifyAvg) / rsaVerifyAvg * 100)));

        comparison.put("sizes", Map.of(
                "signatureSizeRatio", String.format("%.2fx", (double) dilSigSize / rsaSigSize),
                "publicKeySizeRatio", String.format("%.2fx", (double) dilKeySize / rsaKeySize),
                "smallerSignature", rsaSigSize < dilSigSize ? "RSA" : "Dilithium",
                "smallerPublicKey", rsaKeySize < dilKeySize ? "RSA" : "Dilithium"));

        comparison.put("cpuUsage", Map.of(
                "rsaAvgPercent", rsaCpuAvg,
                "dilithiumAvgPercent", dilCpuAvg,
                "ratio", String.format("%.2fx", dilCpuAvg / (rsaCpuAvg > 0 ? rsaCpuAvg : 0.01)),
                "moreEfficient", rsaCpuAvg < dilCpuAvg ? "RSA" : "Dilithium",
                "rsaThreadCpuMs", rsaCpu.get("threadCpuTimeMs"),
                "dilithiumThreadCpuMs", dilCpu.get("threadCpuTimeMs")));

        comparison.put("summary", Map.of(
                "overallFaster", rsaSignAvg + rsaVerifyAvg < dilSignAvg + dilVerifyAvg ? "RSA" : "Dilithium",
                "moreCompact", rsaSigSize + rsaKeySize < dilSigSize + dilKeySize ? "RSA" : "Dilithium",
                "lowerCpuUsage", rsaCpuAvg < dilCpuAvg ? "RSA" : "Dilithium",
                "quantumResistant", "Dilithium",
                "recommendation", "Dilithium para segurança a longo prazo, RSA para compatibilidade atual"));

        return comparison;
    }

    private Map<String, Object> getSecurityAnalysis() {
        return Map.of(
                "rsa2048", Map.of(
                        "classicalSecurity", "112 bits",
                        "quantumSecurity", "0 bits (vulnerável ao algoritmo de Shor)",
                        "estimatedQuantumBreakYear", "2030-2040",
                        "nistRecommendation", "Migrar para algoritmos pós-quânticos"),
                "dilithium3", Map.of(
                        "classicalSecurity", "192 bits",
                        "quantumSecurity", "192 bits (NIST Level 3)",
                        "estimatedQuantumBreakYear", "Não aplicável (resistente)",
                        "nistRecommendation", "Recomendado para uso a longo prazo (FIPS 204)"),
                "openFinanceBrasil", Map.of(
                        "currentRequirement", "RSA-2048 ou superior",
                        "futureRequirement", "Provável migração para PQC até 2030",
                        "tccContribution", "Demonstração de viabilidade técnica da migração"));
    }

    private int calculateApproximateJwtSize(int payloadSize, int signatureSize) {
        int headerBase64 = 48;
        int payloadBase64 = (int) Math.ceil(payloadSize * 4.0 / 3);
        int signatureBase64 = (int) Math.ceil(signatureSize * 4.0 / 3);
        return headerBase64 + payloadBase64 + signatureBase64 + 2;
    }

    private byte[] generateTestPayload(int size) {
        byte[] data = new byte[size];
        new SecureRandom().nextBytes(data);
        return data;
    }

    private double calculateAverage(List<Long> values) {
        return values.stream().mapToLong(Long::longValue).average().orElse(0);
    }

    private double calculateAverageLong(List<Long> values) {
        return values.stream().mapToLong(Long::longValue).average().orElse(0);
    }

    private double calculateAverageDouble(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    private long percentile(List<Long> values, int percentile) {
        List<Long> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int index = (int) Math.ceil(percentile / 100.0 * sorted.size()) - 1;
        return sorted.get(Math.max(0, index));
    }

    private int calculateThroughput(List<Long> timesInMicroseconds) {
        double avgMicroseconds = calculateAverage(timesInMicroseconds);
        if (avgMicroseconds == 0)
            return 0;
        return (int) (1_000_000 / avgMicroseconds);
    }
}