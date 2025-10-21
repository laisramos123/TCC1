package com.example.auth_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.auth_server.dilithium.DilithiumSignature;

@Configuration
public class DilithiumConfig {

    /**
     * Bean para injeção de dependência do DilithiumSignature
     */
    @Bean
    public DilithiumSignature dilithiumSignature() {
        // Usa Dilithium3 como padrão (192-bit segurança)
        return DilithiumSignature.SecurityLevels.level3();
    }
}
