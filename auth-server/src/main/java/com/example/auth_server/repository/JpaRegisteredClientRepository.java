package com.example.auth_server.repository;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

import com.example.auth_server.model.Client;

@Component
@Primary
public class JpaRegisteredClientRepository implements RegisteredClientRepository {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    public JpaRegisteredClientRepository(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        Client client = toEntity(registeredClient);
        clientRepository.save(client);
    }

    @Override
    public RegisteredClient findById(String id) {
        return clientRepository.findById(Long.valueOf(id))
                .map(this::toRegisteredClient)
                .orElse(null);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return clientRepository.findByClientId(clientId)
                .map(this::toRegisteredClient)
                .orElse(null);
    }

    private RegisteredClient toRegisteredClient(Client client) {
        Set<String> redirectUris = client.getRedirectUris();
        Set<String> scopes = client.getScopes();
        Set<String> grantTypes = client.getGrantTypes();

        RegisteredClient.Builder builder = RegisteredClient.withId(client.getId().toString())
                .clientId(client.getClientId())
                .clientSecret(client.getClientSecret())
                .clientName(client.getClientName())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST);

        if (redirectUris != null) {
            redirectUris.forEach(builder::redirectUri);
        }

        if (grantTypes != null) {
            grantTypes.forEach(grantType -> {
                switch (grantType) {
                    case "authorization_code":
                        builder.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE);
                        break;
                    case "refresh_token":
                        builder.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN);
                        break;
                    case "client_credentials":
                        builder.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS);
                        break;
                }
            });
        }

        if (scopes != null) {
            scopes.forEach(builder::scope);
        }

        return builder
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(true)
                        .requireProofKey(false)
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofHours(1))
                        .refreshTokenTimeToLive(Duration.ofDays(30))
                        .reuseRefreshTokens(false)
                        .build())
                .build();
    }

    private Client toEntity(RegisteredClient registeredClient) {
        Client client = new Client();
        client.setClientId(registeredClient.getClientId());
        client.setClientSecret(registeredClient.getClientSecret());
        client.setClientName(registeredClient.getClientName());
        client.setRedirectUris(registeredClient.getRedirectUris());

        Set<String> scopes = registeredClient.getScopes();
        client.setScopes(scopes);

        Set<String> grantTypes = registeredClient.getAuthorizationGrantTypes().stream()
                .map(AuthorizationGrantType::getValue)
                .collect(Collectors.toSet());
        client.setGrantTypes(grantTypes);

        return client;
    }
}