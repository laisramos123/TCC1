package com.example.auth_server.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class DilithiumMetrics {

    private final Counter signatureCounter;
    private final Counter verificationCounter;
    private final Counter consentCreatedCounter;
    private final Counter consentAuthorizedCounter;
    private final Counter consentRejectedCounter;
    private final Timer signatureTimer;
    private final Timer verificationTimer;
    private final Timer keyGenerationTimer;

    public DilithiumMetrics(MeterRegistry registry) {

        this.signatureCounter = Counter.builder("dilithium.signatures.total")
                .description("Total de assinaturas Dilithium criadas")
                .tag("algorithm", "dilithium3")
                .register(registry);

        this.verificationCounter = Counter.builder("dilithium.verifications.total")
                .description("Total de verificações Dilithium realizadas")
                .tag("algorithm", "dilithium3")
                .register(registry);

        this.consentCreatedCounter = Counter.builder("openbanking.consent.created.total")
                .description("Total de consentimentos criados")
                .register(registry);

        this.consentAuthorizedCounter = Counter.builder("openbanking.consent.authorized.total")
                .description("Total de consentimentos autorizados")
                .register(registry);

        this.consentRejectedCounter = Counter.builder("openbanking.consent.rejected.total")
                .description("Total de consentimentos rejeitados")
                .register(registry);

        this.signatureTimer = Timer.builder("dilithium.signature.duration")
                .description("Tempo para criar assinatura Dilithium")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);

        this.verificationTimer = Timer.builder("dilithium.verification.duration")
                .description("Tempo para verificar assinatura Dilithium")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);

        this.keyGenerationTimer = Timer.builder("dilithium.keygen.duration")
                .description("Tempo para gerar par de chaves Dilithium")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }

    public void recordSignature(long durationMs) {
        signatureCounter.increment();
        signatureTimer.record(Duration.ofMillis(durationMs));
    }

    public void recordVerification(long durationMs) {
        verificationCounter.increment();
        verificationTimer.record(Duration.ofMillis(durationMs));
    }

    public void recordKeyGeneration(long durationMs) {
        keyGenerationTimer.record(Duration.ofMillis(durationMs));
    }

    public void recordConsentCreated() {
        consentCreatedCounter.increment();
    }

    public void recordConsentAuthorized() {
        consentAuthorizedCounter.increment();
    }

    public void recordConsentRejected() {
        consentRejectedCounter.increment();
    }
}