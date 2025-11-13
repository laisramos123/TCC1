package com.example.auth_server.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.interceptor.KeyGenerator;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // Cache para consentimentos (TTL: 5 minutos)
        cacheManager.registerCustomCache("consents",
                Caffeine.newBuilder()
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .maximumSize(1000)
                        .recordStats()
                        .build());

        // Cache para tokens JWT (TTL: 30 minutos)
        cacheManager.registerCustomCache("tokens",
                Caffeine.newBuilder()
                        .expireAfterWrite(30, TimeUnit.MINUTES)
                        .maximumSize(5000)
                        .recordStats()
                        .build());

        // Cache para chaves públicas Dilithium (TTL: 1 hora)
        cacheManager.registerCustomCache("dilithiumKeys",
                Caffeine.newBuilder()
                        .expireAfterWrite(1, TimeUnit.HOURS)
                        .maximumSize(100)
                        .recordStats()
                        .build());

        // Cache para validações de consentimento (TTL: 1 minuto)
        cacheManager.registerCustomCache("consentValidations",
                Caffeine.newBuilder()
                        .expireAfterWrite(1, TimeUnit.MINUTES)
                        .maximumSize(2000)
                        .recordStats()
                        .build());

        return cacheManager;
    }

    @Bean("consentKeyGenerator")
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            return method.getName() + "_" + Arrays.toString(params);
        };
    }
}