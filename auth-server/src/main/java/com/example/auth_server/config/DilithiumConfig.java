package com.example.auth_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.example.auth_server.dilithium.DilithiumSignature;

@Configuration
public class DilithiumConfig {

    @Bean("dilithiumService")
    @Primary
    public DilithiumSignature dilithiumSignature() {

        return DilithiumSignature.SecurityLevels.level3();
    }
}
