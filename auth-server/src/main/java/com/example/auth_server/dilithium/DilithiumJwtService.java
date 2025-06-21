package com.example.auth_server.dilithium;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class DilithiumJwtService {

    private final DilithiumKeyGeneratorService keyGeneratorService;
    private KeyPair currentKeyPair;

    static {
        Security.addProvider(new BouncyCastlePQCProvider());
    }

    /**
     * Inicializa o serviço com um par de chaves
     */
    public void initialize() {
        this.currentKeyPair = keyGeneratorService.generateDilithiumKeyPair();
        log.info("🔐 Serviço JWT Dilithium inicializado");
    }

    /**
     * Cria e assina um JWT usando Dilithium
     */
    public String createSignedJWT(Map<String, Object> claims, String subject, String issuer, long expirationMinutes) {
        try {
            if (currentKeyPair == null) {
                initialize();
            }

            // Construir o header JWT
            JWSHeader header = new JWSHeader.Builder(new JWSAlgorithm("Dilithium3"))
                    .type(JOSEObjectType.JWT)
                    .build();

            // Construir os claims
            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                    .subject(subject)
                    .issuer(issuer)
                    .issueTime(new Date())
                    .expirationTime(Date.from(Instant.now().plus(expirationMinutes, ChronoUnit.MINUTES)))
                    .jwtID(java.util.UUID.randomUUID().toString());

            // Adicionar claims customizados
            claims.forEach(claimsBuilder::claim);

            JWTClaimsSet claimsSet = claimsBuilder.build();

            // Criar o JWT
            SignedJWT signedJWT = new SignedJWT(header, claimsSet);

            // Assinar com Dilithium
            byte[] signature = signWithDilithium(signedJWT.getSigningInput(), currentKeyPair.getPrivate());

            // Adicionar a assinatura ao JWT
            signedJWT = new SignedJWT(
                    signedJWT.getHeader().toBase64URL(),
                    signedJWT.getPayload().toBase64URL(),
                    Base64URL.encode(signature));

            String jwt = signedJWT.serialize();
            log.debug("✅ JWT criado e assinado com Dilithium: {}", jwt);

            return jwt;

        } catch (Exception e) {
            log.error("❌ Erro ao criar JWT com Dilithium", e);
            throw new RuntimeException("Falha ao criar JWT", e);
        }
    }

    /**
     * Verifica e decodifica um JWT assinado com Dilithium
     */
    public JWTClaimsSet verifyAndDecodeJWT(String jwt) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(jwt);

            // Verificar a assinatura
            boolean isValid = verifyDilithiumSignature(
                    signedJWT.getSigningInput(),
                    signedJWT.getSignature().decode(),
                    currentKeyPair.getPublic());

            if (!isValid) {
                throw new JOSEException("Assinatura Dilithium inválida");
            }

            // Verificar expiração
            Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (expiration != null && expiration.before(new Date())) {
                throw new JOSEException("Token expirado");
            }

            log.debug("✅ JWT verificado com sucesso");
            return signedJWT.getJWTClaimsSet();

        } catch (Exception e) {
            log.error("❌ Erro ao verificar JWT", e);
            throw new RuntimeException("Falha ao verificar JWT", e);
        }
    }

    /**
     * Assina dados usando Dilithium
     */
    private byte[] signWithDilithium(byte[] data, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("Dilithium", "BCPQC");
        signature.initSign(privateKey);
        signature.update(data);
        return signature.sign();
    }

    /**
     * Verifica assinatura Dilithium
     */
    private boolean verifyDilithiumSignature(byte[] data, byte[] signature, PublicKey publicKey) throws Exception {
        Signature sig = Signature.getInstance("Dilithium", "BCPQC");
        sig.initVerify(publicKey);
        sig.update(data);
        return sig.verify(signature);
    }

    /**
     * Obtém a chave pública atual em Base64
     */
    public String getCurrentPublicKeyBase64() {
        if (currentKeyPair == null) {
            initialize();
        }
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(currentKeyPair.getPublic().getEncoded());
    }

    /**
     * Obtém o JWK da chave atual
     */
    public DilithiumKeyGeneratorService.DilithiumJWK getCurrentJWK() {
        if (currentKeyPair == null) {
            initialize();
        }
        return keyGeneratorService.createDilithiumJWK(currentKeyPair);
    }

    /**
     * Decodifica uma chave pública Dilithium de Base64
     */
    public PublicKey decodePublicKey(String base64Key) throws Exception {
        byte[] keyBytes = Base64.getUrlDecoder().decode(base64Key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("Dilithium", "BCPQC");
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * Decodifica uma chave privada Dilithium de Base64
     */
    public PrivateKey decodePrivateKey(String base64Key) throws Exception {
        byte[] keyBytes = Base64.getUrlDecoder().decode(base64Key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("Dilithium", "BCPQC");
        return keyFactory.generatePrivate(keySpec);
    }
}
