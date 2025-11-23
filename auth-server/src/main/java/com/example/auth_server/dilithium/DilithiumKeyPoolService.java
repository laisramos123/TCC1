package com.example.auth_server.dilithium;

import org.bouncycastle.pqc.jcajce.spec.DilithiumParameterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class DilithiumKeyPoolService {
    private static final Logger log = LoggerFactory.getLogger(DilithiumKeyPoolService.class);

    @Value("${dilithium.key.pool.size:10}")
    private int poolSize;

    @Value("${dilithium.key.pool.min:5}")
    private int minPoolSize;

    @Value("${dilithium.precompute.keys:true}")
    private boolean precomputeKeys;

    @Value("${dilithium.cache.enabled:true}")
    private boolean cacheEnabled;

    private final BlockingQueue<KeyPair> keyPool = new LinkedBlockingQueue<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final ExecutorService keyGenerator = Executors.newFixedThreadPool(2);
    private final AtomicInteger activeKeys = new AtomicInteger(0);

    private final RedisTemplate<String, String> redisTemplate;
    private KeyPair primaryKeyPair;
    private final DilithiumParameterSpec parameterSpec;

    public DilithiumKeyPoolService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.parameterSpec = DilithiumParameterSpec.dilithium3;
    }

    @PostConstruct
    public void init() {
        log.info("Inicializando Dilithium Key Pool Service...");
        loadOrGeneratePrimaryKey();

        if (precomputeKeys) {
            startKeyPoolGeneration();
            scheduler.scheduleWithFixedDelay(
                    this::replenishKeyPool,
                    30, 30, TimeUnit.SECONDS);
        }

        log.info("Dilithium Key Pool Service inicializado com sucesso");
    }

    private void loadOrGeneratePrimaryKey() {
        try {
            if (cacheEnabled && redisTemplate != null) {
                String cachedPrivateKey = redisTemplate.opsForValue().get("dilithium:primary:private");
                String cachedPublicKey = redisTemplate.opsForValue().get("dilithium:primary:public");

                if (cachedPrivateKey != null && cachedPublicKey != null) {
                    primaryKeyPair = reconstructKeyPair(cachedPrivateKey, cachedPublicKey);
                    log.info("Chave primária carregada do cache Redis");
                    return;
                }
            }

            log.info("Gerando nova chave primária Dilithium3...");
            long startTime = System.currentTimeMillis();

            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("Dilithium", "BCPQC");
            keyGen.initialize(parameterSpec, new SecureRandom());
            primaryKeyPair = keyGen.generateKeyPair();

            long duration = System.currentTimeMillis() - startTime;
            log.info("Chave primária gerada em {} ms", duration);

            if (cacheEnabled && redisTemplate != null) {
                cachePrimaryKey();
            }

        } catch (Exception e) {
            log.error("Erro ao inicializar chave primária", e);
            throw new RuntimeException("Falha ao inicializar Dilithium", e);
        }
    }

    private void cachePrimaryKey() {
        try {
            String privateKeyBase64 = Base64.getEncoder().encodeToString(
                    primaryKeyPair.getPrivate().getEncoded());
            String publicKeyBase64 = Base64.getEncoder().encodeToString(
                    primaryKeyPair.getPublic().getEncoded());

            redisTemplate.opsForValue().set("dilithium:primary:private", privateKeyBase64);
            redisTemplate.opsForValue().set("dilithium:primary:public", publicKeyBase64);
            redisTemplate.expire("dilithium:primary:private", 24, TimeUnit.HOURS);
            redisTemplate.expire("dilithium:primary:public", 24, TimeUnit.HOURS);

        } catch (Exception e) {
            log.error("Erro ao cachear chave primária", e);
        }
    }

    private void startKeyPoolGeneration() {
        keyGenerator.submit(() -> {
            log.info("Iniciando geração do pool de chaves...");
            for (int i = 0; i < poolSize; i++) {
                try {
                    generateAndAddKey();
                } catch (Exception e) {
                    log.error("Erro ao gerar chave para o pool", e);
                }
            }
            log.info("Pool inicial de {} chaves gerado", activeKeys.get());
        });
    }

    private void generateAndAddKey() throws Exception {
        if (activeKeys.get() >= poolSize) {
            return;
        }

        long startTime = System.currentTimeMillis();

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("Dilithium", "BCPQC");
        keyGen.initialize(parameterSpec, new SecureRandom());
        KeyPair keyPair = keyGen.generateKeyPair();

        keyPool.offer(keyPair);
        activeKeys.incrementAndGet();

        long duration = System.currentTimeMillis() - startTime;
        log.debug("Chave adicional gerada em {} ms (Pool: {}/{})",
                duration, activeKeys.get(), poolSize);

        if (cacheEnabled && redisTemplate != null) {
            cacheKeyPair(keyPair);
        }
    }

    private void cacheKeyPair(KeyPair keyPair) {
        try {
            String keyId = "dilithium:pool:" + System.currentTimeMillis();
            String privateKeyBase64 = Base64.getEncoder().encodeToString(
                    keyPair.getPrivate().getEncoded());
            String publicKeyBase64 = Base64.getEncoder().encodeToString(
                    keyPair.getPublic().getEncoded());

            redisTemplate.opsForHash().put(keyId, "private", privateKeyBase64);
            redisTemplate.opsForHash().put(keyId, "public", publicKeyBase64);
            redisTemplate.expire(keyId, 1, TimeUnit.HOURS);

        } catch (Exception e) {
            log.error("Erro ao cachear key pair", e);
        }
    }

    private void replenishKeyPool() {
        try {
            int currentSize = activeKeys.get();
            if (currentSize < minPoolSize) {
                int toGenerate = poolSize - currentSize;
                log.info("Reabastecendo pool de chaves. Gerando {} chaves", toGenerate);

                for (int i = 0; i < toGenerate; i++) {
                    keyGenerator.submit(() -> {
                        try {
                            generateAndAddKey();
                        } catch (Exception e) {
                            log.error("Erro ao reabastecer pool", e);
                        }
                    });
                }
            }
        } catch (Exception e) {
            log.error("Erro no reabastecimento do pool", e);
        }
    }

    public KeyPair getKeyPairFromPool() {
        try {
            KeyPair keyPair = keyPool.poll(100, TimeUnit.MILLISECONDS);
            if (keyPair != null) {
                activeKeys.decrementAndGet();
                log.debug("Chave obtida do pool (Restantes: {})", activeKeys.get());

                if (activeKeys.get() < minPoolSize) {
                    scheduler.submit(this::replenishKeyPool);
                }

                return keyPair;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.debug("Pool vazio, usando chave primária");
        return primaryKeyPair;
    }

    @Cacheable(value = "signatures", key = "#data.hashCode()")
    public byte[] sign(byte[] data) throws Exception {
        Signature signature = Signature.getInstance("Dilithium", "BCPQC");
        signature.initSign(primaryKeyPair.getPrivate());
        signature.update(data);
        return signature.sign();
    }

    public boolean verify(byte[] data, byte[] signatureBytes, PublicKey publicKey) throws Exception {
        Signature signature = Signature.getInstance("Dilithium", "BCPQC");
        signature.initVerify(publicKey);
        signature.update(data);
        return signature.verify(signatureBytes);
    }

    public PublicKey getPrimaryPublicKey() {
        return primaryKeyPair.getPublic();
    }

    public PrivateKey getPrimaryPrivateKey() {
        return primaryKeyPair.getPrivate();
    }

    private KeyPair reconstructKeyPair(String privateKeyBase64, String publicKeyBase64) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("Dilithium", "BCPQC");

        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64);
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

        return new KeyPair(publicKey, privateKey);
    }

    public PoolStatistics getStatistics() {
        PoolStatistics stats = new PoolStatistics();
        stats.setCurrentSize(activeKeys.get());
        stats.setMaxSize(poolSize);
        stats.setMinSize(minPoolSize);
        stats.setTotalGenerated(0);
        stats.setTotalReused(0);
        stats.setAverageGenerationTime(0.0);
        stats.setLastRefillTime(Instant.now());
        return stats;
    }

    @PreDestroy
    public void shutdown() {
        log.info("Encerrando Dilithium Key Pool Service...");
        scheduler.shutdown();
        keyGenerator.shutdown();

        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            if (!keyGenerator.awaitTermination(5, TimeUnit.SECONDS)) {
                keyGenerator.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        keyPool.clear();
        log.info("Dilithium Key Pool Service encerrado");
    }

    public static class PoolStatistics {
        private int currentSize;
        private int maxSize;
        private int minSize;
        private long totalGenerated;
        private long totalReused;
        private double averageGenerationTime;
        private Instant lastRefillTime;
        
        // Getters e Setters
        public int getCurrentSize() { return currentSize; }
        public void setCurrentSize(int currentSize) { this.currentSize = currentSize; }
        
        public int getMaxSize() { return maxSize; }
        public void setMaxSize(int maxSize) { this.maxSize = maxSize; }
        
        public int getMinSize() { return minSize; }
        public void setMinSize(int minSize) { this.minSize = minSize; }
        
        public long getTotalGenerated() { return totalGenerated; }
        public void setTotalGenerated(long totalGenerated) { this.totalGenerated = totalGenerated; }
        
        public long getTotalReused() { return totalReused; }
        public void setTotalReused(long totalReused) { this.totalReused = totalReused; }
        
        public double getAverageGenerationTime() { return averageGenerationTime; }
        public void setAverageGenerationTime(double averageGenerationTime) { this.averageGenerationTime = averageGenerationTime; }
        
        public Instant getLastRefillTime() { return lastRefillTime; }
        public void setLastRefillTime(Instant lastRefillTime) { this.lastRefillTime = lastRefillTime; }
    }
}
