package com.example.auth_server.metrics;

import com.example.auth_server.signature.SignatureAlgorithm;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class SignatureMetricsExporter {

        private final MeterRegistry meterRegistry;
        private final SignatureAlgorithm rsaAlgorithm;
        private final SignatureAlgorithm dilithiumAlgorithm;

        private Counter rsaSignCounter;
        private Counter rsaVerifyCounter;
        private Counter dilithiumSignCounter;
        private Counter dilithiumVerifyCounter;

        private Timer rsaSignTimer;
        private Timer rsaVerifyTimer;
        private Timer dilithiumSignTimer;
        private Timer dilithiumVerifyTimer;

        private final AtomicLong rsaKeyGenTime = new AtomicLong(0);
        private final AtomicLong dilithiumKeyGenTime = new AtomicLong(0);
        private final AtomicLong rsaSignatureSize = new AtomicLong(0);
        private final AtomicLong dilithiumSignatureSize = new AtomicLong(0);
        private final AtomicLong rsaPublicKeySize = new AtomicLong(0);
        private final AtomicLong dilithiumPublicKeySize = new AtomicLong(0);

        public SignatureMetricsExporter(
                        MeterRegistry meterRegistry,
                        @Qualifier("rsaSignature") SignatureAlgorithm rsaAlgorithm,
                        @Qualifier("dilithiumAlgorithm") SignatureAlgorithm dilithiumAlgorithm) {
                this.meterRegistry = meterRegistry;
                this.rsaAlgorithm = rsaAlgorithm;
                this.dilithiumAlgorithm = dilithiumAlgorithm;
        }

        @PostConstruct
        public void init() {

                rsaSignCounter = Counter.builder("signature.operations.total")
                                .tag("algorithm", "RSA")
                                .tag("operation", "sign")
                                .description("Total de operações de assinatura RSA")
                                .register(meterRegistry);

                rsaVerifyCounter = Counter.builder("signature.operations.total")
                                .tag("algorithm", "RSA")
                                .tag("operation", "verify")
                                .description("Total de operações de verificação RSA")
                                .register(meterRegistry);

                dilithiumSignCounter = Counter.builder("signature.operations.total")
                                .tag("algorithm", "DILITHIUM")
                                .tag("operation", "sign")
                                .description("Total de operações de assinatura Dilithium")
                                .register(meterRegistry);

                dilithiumVerifyCounter = Counter.builder("signature.operations.total")
                                .tag("algorithm", "DILITHIUM")
                                .tag("operation", "verify")
                                .description("Total de operações de verificação Dilithium")
                                .register(meterRegistry);

                rsaSignTimer = Timer.builder("signature.duration.seconds")
                                .tag("algorithm", "RSA")
                                .tag("operation", "sign")
                                .description("Tempo de assinatura RSA")
                                .register(meterRegistry);

                rsaVerifyTimer = Timer.builder("signature.duration.seconds")
                                .tag("algorithm", "RSA")
                                .tag("operation", "verify")
                                .description("Tempo de verificação RSA")
                                .register(meterRegistry);

                dilithiumSignTimer = Timer.builder("signature.duration.seconds")
                                .tag("algorithm", "DILITHIUM")
                                .tag("operation", "sign")
                                .description("Tempo de assinatura Dilithium")
                                .register(meterRegistry);

                dilithiumVerifyTimer = Timer.builder("signature.duration.seconds")
                                .tag("algorithm", "DILITHIUM")
                                .tag("operation", "verify")
                                .description("Tempo de verificação Dilithium")
                                .register(meterRegistry);

                Gauge.builder("signature.keygen.time.ms", rsaKeyGenTime, AtomicLong::get)
                                .tag("algorithm", "RSA")
                                .description("Tempo de geração de chave RSA (ms)")
                                .register(meterRegistry);

                Gauge.builder("signature.keygen.time.ms", dilithiumKeyGenTime, AtomicLong::get)
                                .tag("algorithm", "DILITHIUM")
                                .description("Tempo de geração de chave Dilithium (ms)")
                                .register(meterRegistry);

                Gauge.builder("signature.size.bytes", rsaSignatureSize, AtomicLong::get)
                                .tag("algorithm", "RSA")
                                .tag("type", "signature")
                                .description("Tamanho da assinatura RSA (bytes)")
                                .register(meterRegistry);

                Gauge.builder("signature.size.bytes", dilithiumSignatureSize, AtomicLong::get)
                                .tag("algorithm", "DILITHIUM")
                                .tag("type", "signature")
                                .description("Tamanho da assinatura Dilithium (bytes)")
                                .register(meterRegistry);

                Gauge.builder("signature.size.bytes", rsaPublicKeySize, AtomicLong::get)
                                .tag("algorithm", "RSA")
                                .tag("type", "public_key")
                                .description("Tamanho da chave pública RSA (bytes)")
                                .register(meterRegistry);

                Gauge.builder("signature.size.bytes", dilithiumPublicKeySize, AtomicLong::get)
                                .tag("algorithm", "DILITHIUM")
                                .tag("type", "public_key")
                                .description("Tamanho da chave pública Dilithium (bytes)")
                                .register(meterRegistry);

                Gauge.builder("signature.algorithm.active", () -> 1)
                                .tag("algorithm", "RSA")
                                .description("Algoritmo RSA disponível")
                                .register(meterRegistry);

                Gauge.builder("signature.algorithm.active", () -> 1)
                                .tag("algorithm", "DILITHIUM")
                                .description("Algoritmo Dilithium disponível")
                                .register(meterRegistry);
        }

        public void recordRsaSign(long durationNanos, int signatureSize) {
                rsaSignCounter.increment();
                rsaSignTimer.record(durationNanos, TimeUnit.NANOSECONDS);
                rsaSignatureSize.set(signatureSize);
        }

        public void recordRsaVerify(long durationNanos) {
                rsaVerifyCounter.increment();
                rsaVerifyTimer.record(durationNanos, TimeUnit.NANOSECONDS);
        }

        public void recordDilithiumSign(long durationNanos, int signatureSize) {
                dilithiumSignCounter.increment();
                dilithiumSignTimer.record(durationNanos, TimeUnit.NANOSECONDS);
                dilithiumSignatureSize.set(signatureSize);
        }

        public void recordDilithiumVerify(long durationNanos) {
                dilithiumVerifyCounter.increment();
                dilithiumVerifyTimer.record(durationNanos, TimeUnit.NANOSECONDS);
        }

        public void recordRsaKeyGeneration(long durationMs, int publicKeySize) {
                rsaKeyGenTime.set(durationMs);
                rsaPublicKeySize.set(publicKeySize);
        }

        public void recordDilithiumKeyGeneration(long durationMs, int publicKeySize) {
                dilithiumKeyGenTime.set(durationMs);
                dilithiumPublicKeySize.set(publicKeySize);
        }
}