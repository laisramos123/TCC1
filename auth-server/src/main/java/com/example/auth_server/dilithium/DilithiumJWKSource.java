package com.example.auth_server.dilithium;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.example.auth_server.dilithium.DilithiumKeyGeneratorService;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DilithiumJWKSource implements JWKSource<SecurityContext> {

    private final DilithiumKeyGeneratorService keyGeneratorService;

    // Armazena DIRETAMENTE a referência da chave Dilithium (sem cast)
    private DilithiumKeyGeneratorService.DilithiumJWK currentDilithiumJWK;
    private JWKSet jwkSet;

    public DilithiumJWKSource(DilithiumKeyGeneratorService keyGeneratorService) {
        this.keyGeneratorService = keyGeneratorService;
        initializeJWKSet();
    }

    private void initializeJWKSet() {
        try {
            // Gera um par de chaves Dilithium
            var keyPair = keyGeneratorService.generateDilithiumKeyPair();
            var dilithiumJWK = keyGeneratorService.createDilithiumJWK(keyPair);

            // SOLUÇÃO: Armazena a referência DIRETA (evita problemas de cast)
            this.currentDilithiumJWK = dilithiumJWK;

            // Cria o JWKSet - cast para JWK funciona na criação
            this.jwkSet = new JWKSet(List.of((JWK) dilithiumJWK));

            log.info("✅ JWK Set Dilithium inicializado com sucesso. Key ID: {}",
                    dilithiumJWK.getKeyID());

        } catch (Exception e) {
            log.error("❌ Falha ao inicializar JWK Set Dilithium", e);
            throw new RuntimeException("Falha ao inicializar JWK Set Dilithium", e);
        }
    }

    @Override
    public List<JWK> get(JWKSelector jwkSelector, SecurityContext context) {
        try {
            return jwkSelector.select(jwkSet);
        } catch (Exception e) {
            log.error("❌ Erro ao selecionar JWK", e);
            return List.of();
        }
    }

    public JWKSet getJWKSet() {
        return jwkSet;
    }

    /**
     * Rotaciona as chaves, gerando um novo par de chaves Dilithium
     */
    public void rotateKeys() {
        log.info("🔄 Iniciando rotação de chaves Dilithium...");

        try {
            // Gera nova chave
            var keyPair = keyGeneratorService.generateDilithiumKeyPair();
            var newDilithiumJWK = keyGeneratorService.createDilithiumJWK(keyPair);

            // SOLUÇÃO: Atualiza referência direta (sem cast)
            this.currentDilithiumJWK = newDilithiumJWK;

            // Atualiza JWKSet
            this.jwkSet = new JWKSet(List.of((JWK) newDilithiumJWK));

            log.info("✅ Rotação de chaves concluída. Nova chave ID: {}",
                    newDilithiumJWK.getKeyID());

        } catch (Exception e) {
            log.error("❌ Erro durante rotação de chaves", e);
            throw new RuntimeException("Falha na rotação de chaves Dilithium", e);
        }
    }

    /**
     * SOLUÇÃO: Retorna a referência direta (SEM CAST)
     */
    public DilithiumKeyGeneratorService.DilithiumJWK getCurrentDilithiumJWK() {
        if (currentDilithiumJWK != null) {
            return currentDilithiumJWK;
        }
        throw new IllegalStateException("Nenhuma chave Dilithium encontrada");
    }

    /**
     * Método para obter informações da chave sem cast
     */
    public Map<String, Object> getCurrentKeyInfo() {
        Map<String, Object> info = new HashMap<>();

        try {
            var current = getCurrentDilithiumJWK(); // USA método sem cast
            info.put("keyId", current.getKeyID());
            info.put("algorithm", current.getAlgorithm() != null ? current.getAlgorithm().getName() : "Dilithium3");
            info.put("keyType", current.getKeyType().getValue());
            info.put("keyUse", current.getKeyUse() != null ? current.getKeyUse().identifier() : "sig");
            info.put("isPrivate", current.isPrivate());
            info.put("size", current.size());
            info.put("issueTime", current.getIssueTime());
            info.put("className", current.getClass().getSimpleName());

        } catch (Exception e) {
            info.put("error", e.getMessage());
            log.error("Erro ao obter informações da chave", e);
        }

        return info;
    }

    /**
     * Retorna apenas as chaves públicas
     */
    public JWKSet getPublicJWKSet() {
        try {
            // Usa o método sem cast para obter a chave
            var dilithiumJWK = getCurrentDilithiumJWK();
            JWK publicJWK = dilithiumJWK.toPublicJWK();

            return new JWKSet(List.of(publicJWK));

        } catch (Exception e) {
            log.error("❌ Erro ao criar JWK Set público", e);
            return new JWKSet(List.of());
        }
    }

    /**
     * Método para debug que NÃO usa cast
     */
    public void debugJWKSet() {
        if (jwkSet == null) {
            log.warn("JWK Set é null");
            return;
        }

        List<JWK> keys = jwkSet.getKeys();
        log.info("JWK Set contém {} chave(s)", keys.size());

        for (int i = 0; i < keys.size(); i++) {
            JWK key = keys.get(i);
            log.info("Chave {}: Tipo={}, ID={}, Algoritmo={}",
                    i,
                    key.getClass().getSimpleName(),
                    key.getKeyID(),
                    key.getAlgorithm() != null ? key.getAlgorithm().getName() : "null");
        }

        // Info da chave Dilithium usando método sem cast
        try {
            var dilithiumInfo = getCurrentKeyInfo();
            log.info("Informações da chave Dilithium: {}", dilithiumInfo);
        } catch (Exception e) {
            log.error("Erro ao obter info da chave Dilithium", e);
        }
    }

    /**
     * Método utilitário para obter chave por ID sem cast
     */
    public Optional<DilithiumKeyGeneratorService.DilithiumJWK> getDilithiumJWKById(String keyId) {
        try {
            var current = getCurrentDilithiumJWK();
            if (current != null && keyId.equals(current.getKeyID())) {
                return Optional.of(current);
            }
        } catch (Exception e) {
            log.debug("Erro ao buscar chave por ID: {}", keyId, e);
        }

        return Optional.empty();
    }

    /**
     * Método para verificar se o JWK Set está funcionando
     */
    public boolean isHealthy() {
        try {
            return getCurrentDilithiumJWK() != null && jwkSet != null && !jwkSet.getKeys().isEmpty();
        } catch (Exception e) {
            log.debug("Health check falhou", e);
            return false;
        }
    }
}