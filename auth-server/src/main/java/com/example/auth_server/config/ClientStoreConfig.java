package com.example.auth_server.config;

import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

@Configuration
public class ClientStoreConfig {

    @Bean
    RegisteredClientRepository registeredClientRepository() {
        var registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
        .clientId("my-client")
        return new ClientRepository();
    }
}
