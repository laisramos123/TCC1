package com.example.auth_server.controller;

import com.example.auth_server.service.SignatureComparisonService;
import com.example.auth_server.signature.SignatureAlgorithm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/signature")
@Tag(name = "Signature Comparison", description = "APIs para comparação RSA vs Dilithium")
public class SignatureComparisonController {

    private final SignatureComparisonService comparisonService;
    private final SignatureAlgorithm rsaAlgorithm;
    private final SignatureAlgorithm dilithiumAlgorithm;

    public SignatureComparisonController(
            SignatureComparisonService comparisonService,
            @Qualifier("rsaSignature") SignatureAlgorithm rsaAlgorithm,
            @Qualifier("dilithiumAlgorithm") SignatureAlgorithm dilithiumAlgorithm) {
        this.comparisonService = comparisonService;
        this.rsaAlgorithm = rsaAlgorithm;
        this.dilithiumAlgorithm = dilithiumAlgorithm;
    }

    @PostMapping("/compare")
    @Operation(summary = "Compara assinatura RSA vs Dilithium", description = "Assina os dados com ambos os algoritmos e retorna métricas de comparação")
    public ResponseEntity<Map<String, Object>> compare(@RequestBody CompareRequest request) {
        try {
            Map<String, Object> result = comparisonService.compareSignature(request.getData());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Erro na comparação: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/benchmark")
    @Operation(summary = "Executa benchmark com múltiplas iterações", description = "Realiza N iterações de assinatura/verificação para análise estatística")
    public ResponseEntity<Map<String, Object>> benchmark(
            @RequestBody BenchmarkRequest request) {
        try {
            Map<String, Object> result = comparisonService.runBenchmark(
                    request.getData(),
                    request.getIterations());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Erro no benchmark: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/algorithms")
    @Operation(summary = "Lista algoritmos disponíveis", description = "Retorna informações sobre os algoritmos de assinatura disponíveis")
    public ResponseEntity<Map<String, Object>> getAlgorithms() throws Exception {
        Map<String, Object> result = new HashMap<>();

        if (rsaAlgorithm.getPublicKey() == null)
            rsaAlgorithm.generateKeyPair();
        if (dilithiumAlgorithm.getPublicKey() == null)
            dilithiumAlgorithm.generateKeyPair();

        result.put("rsa", Map.of(
                "name", rsaAlgorithm.getAlgorithmName(),
                "jwtHeader", rsaAlgorithm.getJwtAlgorithmHeader(),
                "publicKeySize", rsaAlgorithm.getPublicKey().getEncoded().length,
                "keyGenTimeMs", rsaAlgorithm.getMetrics().getKeyGenerationTime(),
                "quantumResistant", false,
                "openFinanceCompliant", true,
                "standard", "PKCS#1 / RFC 8017"));

        result.put("dilithium", Map.of(
                "name", dilithiumAlgorithm.getAlgorithmName(),
                "jwtHeader", dilithiumAlgorithm.getJwtAlgorithmHeader(),
                "publicKeySize", dilithiumAlgorithm.getPublicKey().getEncoded().length,
                "keyGenTimeMs", dilithiumAlgorithm.getMetrics().getKeyGenerationTime(),
                "quantumResistant", true,
                "openFinanceCompliant", false,
                "standard", "NIST FIPS 204 (ML-DSA)"));

        return ResponseEntity.ok(result);
    }

    @GetMapping("/public-keys")
    @Operation(summary = "Retorna as chaves públicas de ambos os algoritmos", description = "Útil para validação de tokens em outros serviços")
    public ResponseEntity<Map<String, Object>> getPublicKeys() throws Exception {

        if (rsaAlgorithm.getPublicKey() == null)
            rsaAlgorithm.generateKeyPair();
        if (dilithiumAlgorithm.getPublicKey() == null)
            dilithiumAlgorithm.generateKeyPair();

        Map<String, Object> result = new HashMap<>();

        result.put("rsa", Map.of(
                "algorithm", "RSA",
                "format", rsaAlgorithm.getPublicKey().getFormat(),
                "publicKey", Base64.getEncoder().encodeToString(
                        rsaAlgorithm.getPublicKey().getEncoded())));

        result.put("dilithium", Map.of(
                "algorithm", "DILITHIUM",
                "format", dilithiumAlgorithm.getPublicKey().getFormat(),
                "publicKey", Base64.getEncoder().encodeToString(
                        dilithiumAlgorithm.getPublicKey().getEncoded())));

        return ResponseEntity.ok(result);
    }

    @GetMapping("/metrics")
    @Operation(summary = "Retorna métricas acumuladas", description = "Métricas de todas as operações de assinatura realizadas")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        Map<String, Object> result = new HashMap<>();

        result.put("rsa", Map.of(
                "signOperations", rsaAlgorithm.getMetrics().getSignOperations(),
                "verifyOperations", rsaAlgorithm.getMetrics().getVerifyOperations(),
                "avgSignTimeMs", rsaAlgorithm.getMetrics().getAverageSignTime(),
                "avgVerifyTimeMs", rsaAlgorithm.getMetrics().getAverageVerifyTime(),
                "keyGenTimeMs", rsaAlgorithm.getMetrics().getKeyGenerationTime()));

        result.put("dilithium", Map.of(
                "signOperations", dilithiumAlgorithm.getMetrics().getSignOperations(),
                "verifyOperations", dilithiumAlgorithm.getMetrics().getVerifyOperations(),
                "avgSignTimeMs", dilithiumAlgorithm.getMetrics().getAverageSignTime(),
                "avgVerifyTimeMs", dilithiumAlgorithm.getMetrics().getAverageVerifyTime(),
                "keyGenTimeMs", dilithiumAlgorithm.getMetrics().getKeyGenerationTime()));

        return ResponseEntity.ok(result);
    }

    public static class CompareRequest {
        private String data;

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }

    public static class BenchmarkRequest {
        private String data;
        private int iterations = 100;

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        public int getIterations() {
            return iterations;
        }

        public void setIterations(int iterations) {
            this.iterations = iterations;
        }
    }
}