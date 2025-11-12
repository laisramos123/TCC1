package com.example.resource_server.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    @Primary
    public RestTemplate restTemplate(@Qualifier("mtlsRestTemplate") RestTemplate mtlsRestTemplate) {
        return mtlsRestTemplate;
    }
}