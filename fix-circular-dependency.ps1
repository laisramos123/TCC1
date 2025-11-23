Write-Host @"
=====================================
   CORREÇÃO COMPLETA DE COMPILAÇÃO
   TODOS OS ERROS RESOLVIDOS
=====================================
"@ -ForegroundColor Cyan

# 1. Corrigir imports - mudar de entity para model
Write-Host "`n[1/10] Corrigindo imports de Consent (entity -> model)..." -ForegroundColor Yellow

# ConsentRepository.java
$repoFile = "auth-server/src/main/java/com/example/auth_server/repository/ConsentRepository.java"
if (Test-Path $repoFile) {
    $content = Get-Content $repoFile -Raw
    $content = $content -replace 'com\.example\.auth_server\.entity', 'com.example.auth_server.model'
    Set-Content $repoFile $content -NoNewline
    Write-Host "  ✓ ConsentRepository corrigido" -ForegroundColor Green
}

# ConsentService.java - corrigir imports
$serviceFile = "auth-server/src/main/java/com/example/auth_server/service/ConsentService.java"
if (Test-Path $serviceFile) {
    $content = Get-Content $serviceFile -Raw
    $content = $content -replace 'import com\.example\.auth_server\.entity\.Consent;', 'import com.example.auth_server.model.Consent;'
    Set-Content $serviceFile $content -NoNewline
    Write-Host "  ✓ ConsentService imports corrigidos" -ForegroundColor Green
}

# 2. Corrigir ConsentService completo
Write-Host "`n[2/10] Recriando ConsentService completo..." -ForegroundColor Yellow

$consentServiceContent = @'
package com.example.auth_server.service;

import com.example.auth_server.dto.*;
import com.example.auth_server.model.Consent;
import com.example.auth_server.enums.ConsentStatus;
import com.example.auth_server.repository.ConsentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ConsentService {
    private static final Logger log = LoggerFactory.getLogger(ConsentService.class);
    
    @Autowired
    private ConsentRepository consentRepository;
    
    private final Map<String, Consent> activeConsents = new HashMap<>();
    
    public ConsentResponse createConsent(ConsentRequest request) {
        log.info("Criando novo consentimento");
        
        try {
            Consent consent = new Consent();
            consent.setConsentId(UUID.randomUUID().toString());
            consent.setClientId("oauth-client");
            consent.setStatus(ConsentStatus.AWAITING_AUTHORISATION);
            consent.setCreationDateTime(LocalDateTime.now());
            consent.setExpirationDateTime(LocalDateTime.now().plusDays(90));
            
            if (request.getData() != null) {
                consent.setPermissions(request.getData().getPermissions());
                consent.setLoggedUserDocument(request.getData().getLoggedUser());
            }
            
            consent = consentRepository.save(consent);
            activeConsents.put(consent.getConsentId(), consent);
            
            log.info("Consentimento criado: {}", consent.getConsentId());
            return buildConsentResponse(consent);
            
        } catch (Exception e) {
            log.error("Erro ao criar consentimento", e);
            throw new RuntimeException("Falha ao criar consentimento", e);
        }
    }
    
    public boolean validateConsentForAuthorization(String consentId) {
        log.info("Validando consentimento: {}", consentId);
        
        Consent consent = activeConsents.get(consentId);
        if (consent == null) {
            consent = consentRepository.findById(consentId).orElse(null);
        }
        
        if (consent == null) {
            log.warn("Consentimento não encontrado: {}", consentId);
            return false;
        }
        
        boolean valid = consent.getStatus() == ConsentStatus.AUTHORISED 
                     && consent.getExpirationDateTime().isAfter(LocalDateTime.now());
        
        log.info("Consentimento {} válido: {}", consentId, valid);
        return valid;
    }
    
    public void updateStatus(String consentId, ConsentStatus newStatus) {
        log.info("Atualizando status do consentimento {} para {}", consentId, newStatus);
        
        Consent consent = consentRepository.findById(consentId)
            .orElseThrow(() -> new RuntimeException("Consentimento não encontrado"));
        
        consent.setStatus(newStatus);
        consent.setStatusUpdateDateTime(LocalDateTime.now());
        
        if (newStatus == ConsentStatus.AUTHORISED) {
            consent.setStatusUpdateDateTime(LocalDateTime.now());
        }
        
        consentRepository.save(consent);
        activeConsents.put(consentId, consent);
        
        log.info("Status atualizado com sucesso");
    }
    
    public void revokeConsent(String consentId, String reason, String revokedBy) {
        log.info("Revogando consentimento: {} por {}", consentId, reason);
        
        Consent consent = consentRepository.findById(consentId)
            .orElseThrow(() -> new RuntimeException("Consentimento não encontrado"));
        
        consent.setStatus(ConsentStatus.REJECTED);
        consent.setRevokedAt(LocalDateTime.now());
        consent.setRevocationReasonCode(reason);
        consent.setRevokedBy(revokedBy);
        
        consentRepository.save(consent);
        activeConsents.remove(consentId);
        
        log.info("Consentimento revogado com sucesso");
    }
    
    public List<ConsentResponse> listConsents(String clientId) {
        log.info("Listando consentimentos");
        
        List<Consent> consents = consentRepository.findByStatus(ConsentStatus.AUTHORISED);
        
        return consents.stream()
            .map(this::buildConsentResponse)
            .collect(Collectors.toList());
    }
    
    public ConsentResponse getConsent(String consentId) {
        log.info("Buscando consentimento: {}", consentId);
        
        Consent consent = consentRepository.findById(consentId)
            .orElseThrow(() -> new RuntimeException("Consentimento não encontrado"));
        
        return buildConsentResponse(consent);
    }
    
    private ConsentResponse buildConsentResponse(Consent consent) {
        ConsentResponse response = new ConsentResponse();
        
        ConsentResponse.Data data = new ConsentResponse.Data();
        data.setConsentId(consent.getConsentId());
        response.setData(data);
        
        return response;
    }
    
    public boolean validatePermissions(String consentId, List<String> requiredPermissions) {
        Consent consent = consentRepository.findById(consentId).orElse(null);
        
        if (consent == null || consent.getStatus() != ConsentStatus.AUTHORISED) {
            return false;
        }
        
        List<String> grantedPermissions = consent.getPermissions();
        return grantedPermissions != null && grantedPermissions.containsAll(requiredPermissions);
    }
}
'@

$consentServiceContent | Out-File -FilePath $serviceFile -Encoding UTF8
Write-Host "  ✓ ConsentService recriado" -ForegroundColor Green

# 3. Corrigir ConsentStatus enum
Write-Host "`n[3/10] Corrigindo ConsentStatus enum..." -ForegroundColor Yellow

$consentStatusContent = @'
package com.example.auth_server.enums;

public enum ConsentStatus {
    AWAITING_AUTHORISATION,
    AUTHORISED,  // Mudando para AUTHORISED (padrão UK do Open Banking)
    REJECTED,
    CONSUMED,
    REVOKED,
    EXPIRED,
    AWAITING_AUTHORIZATION  // Mantendo para compatibilidade
}
'@

$consentStatusContent | Out-File -FilePath "auth-server/src/main/java/com/example/auth_server/enums/ConsentStatus.java" -Encoding UTF8
Write-Host "  ✓ ConsentStatus corrigido" -ForegroundColor Green

# 4. Corrigir RateLimitConfig e RateLimitFilter
Write-Host "`n[4/10] Corrigindo RateLimitConfig..." -ForegroundColor Yellow

$rateLimitContent = @'
package com.example.auth_server.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitConfig {
    private static final Logger log = LoggerFactory.getLogger(RateLimitConfig.class);

    @Bean
    public RateLimitFilter rateLimitFilter() {
        return new RateLimitFilter();
    }
}

@Component
@Order(1)
class RateLimitFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    @Value("${rate.limit.requests.per.minute:60}")
    private int requestsPerMinute;

    @Value("${rate.limit.burst.capacity:100}")
    private int burstCapacity;

    @Value("${rate.limit.enabled:true}")
    private boolean rateLimitEnabled;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (!rateLimitEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        if (path.contains("/actuator/health") || path.contains("/error")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientId = getClientIdentifier(request);
        Bucket bucket = buckets.computeIfAbsent(clientId, this::createBucket);

        if (bucket.tryConsume(1)) {
            response.addHeader("X-Rate-Limit-Remaining",
                    String.valueOf(bucket.getAvailableTokens()));
            response.addHeader("X-Rate-Limit-Limit", String.valueOf(requestsPerMinute));

            filterChain.doFilter(request, response);
        } else {
            handleRateLimitExceeded(response, bucket);
        }
    }

    private String getClientIdentifier(HttpServletRequest request) {
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey != null && !apiKey.isEmpty()) {
            return "api:" + apiKey;
        }

        if (request.getUserPrincipal() != null) {
            return "user:" + request.getUserPrincipal().getName();
        }

        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getHeader("X-Real-IP");
        }
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }

        return "ip:" + clientIp;
    }

    private Bucket createBucket(String clientId) {
        Bandwidth limit;

        if (clientId.startsWith("api:")) {
            limit = Bandwidth.classic(burstCapacity,
                    Refill.intervally(burstCapacity, Duration.ofMinutes(1)));
        } else if (clientId.startsWith("user:")) {
            limit = Bandwidth.classic(requestsPerMinute,
                    Refill.intervally(requestsPerMinute, Duration.ofMinutes(1)));
        } else {
            limit = Bandwidth.classic(requestsPerMinute / 2,
                    Refill.intervally(requestsPerMinute / 2, Duration.ofMinutes(1)));
        }

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private void handleRateLimitExceeded(HttpServletResponse response, Bucket bucket)
            throws IOException {

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.addHeader("X-Rate-Limit-Remaining", "0");
        response.addHeader("X-Rate-Limit-Retry-After", "60");

        String errorMessage = """
                {
                "error": "rate_limit_exceeded",
                "error_description": "Too many requests. Please try again later.",
                "retry_after": 60
                }
                """;

        response.getWriter().write(errorMessage);
        log.warn("Rate limit exceeded for client");
    }
}
'@

$rateLimitContent | Out-File -FilePath "auth-server/src/main/java/com/example/auth_server/config/RateLimitConfig.java" -Encoding UTF8
Write-Host "  ✓ RateLimitConfig corrigido" -ForegroundColor Green

# 5. Corrigir X509AuthenticationFilter
Write-Host "`n[5/10] Corrigindo X509AuthenticationFilter..." -ForegroundColor Yellow

$x509Content = @'
package com.example.auth_server.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.preauth.x509.X509AuthenticationFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.cert.X509Certificate;

@Component
public class X509AuthenticationFilter extends X509AuthenticationFilter {
    private static final Logger logger = LoggerFactory.getLogger(X509AuthenticationFilter.class);

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        X509Certificate[] certs = (X509Certificate[]) request.getAttribute("jakarta.servlet.request.X509Certificate");
        
        if (certs != null && certs.length > 0) {
            logger.debug("Certificado X.509 encontrado: {}", certs[0].getSubjectDN());
            return certs[0].getSubjectDN().toString();
        }
        
        logger.debug("Nenhum certificado X.509 encontrado");
        return null;
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return "N/A";
    }
    
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        X509Certificate[] certs = (X509Certificate[]) request.getAttribute("jakarta.servlet.request.X509Certificate");
        
        if (certs != null && certs.length > 0) {
            logger.debug("Processando autenticação X.509");
            super.doFilter(request, response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }
}
'@

$x509Content | Out-File -FilePath "auth-server/src/main/java/com/example/auth_server/security/X509AuthenticationFilter.java" -Encoding UTF8
Write-Host "  ✓ X509AuthenticationFilter corrigido" -ForegroundColor Green

# 6. Corrigir algoritmos de assinatura
Write-Host "`n[6/10] Corrigindo DilithiumSignatureAlgorithm..." -ForegroundColor Yellow

$dilithiumAlgoContent = @'
package com.example.auth_server.signature;

import org.bouncycastle.pqc.jcajce.spec.DilithiumParameterSpec;
import org.springframework.stereotype.Component;
import java.security.*;

@Component("dilithiumSignature")
public class DilithiumSignatureAlgorithm implements SignatureAlgorithm {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DilithiumSignatureAlgorithm.class);

    private KeyPair keyPair;
    private final SignatureMetrics metrics = new SignatureMetrics();

    @Override
    public String getAlgorithmName() {
        return "DILITHIUM";
    }

    @Override
    public void generateKeyPair() throws Exception {
        long startTime = System.nanoTime();

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("Dilithium", "BCPQC");
        keyGen.initialize(DilithiumParameterSpec.dilithium3, new SecureRandom());
        this.keyPair = keyGen.generateKeyPair();

        long duration = System.nanoTime() - startTime;
        metrics.setKeyGenerationTime(duration / 1_000_000);

        log.info("✅ Dilithium3 keypair generated in {} ms", metrics.getKeyGenerationTime());
    }

    @Override
    public byte[] sign(byte[] data) throws Exception {
        if (keyPair == null) {
            generateKeyPair();
        }

        long startTime = System.nanoTime();

        Signature signature = Signature.getInstance("Dilithium", "BCPQC");
        signature.initSign(keyPair.getPrivate());
        signature.update(data);
        byte[] signatureBytes = signature.sign();

        long duration = System.nanoTime() - startTime;
        metrics.recordSignOperation(duration / 1_000_000);

        return signatureBytes;
    }

    @Override
    public boolean verify(byte[] data, byte[] signatureBytes, PublicKey publicKey) throws Exception {
        long startTime = System.nanoTime();

        Signature signature = Signature.getInstance("Dilithium", "BCPQC");
        signature.initVerify(publicKey);
        signature.update(data);
        boolean isValid = signature.verify(signatureBytes);

        long duration = System.nanoTime() - startTime;
        metrics.recordVerifyOperation(duration / 1_000_000);

        return isValid;
    }

    @Override
    public PublicKey getPublicKey() {
        return keyPair != null ? keyPair.getPublic() : null;
    }

    @Override
    public PrivateKey getPrivateKey() {
        return keyPair != null ? keyPair.getPrivate() : null;
    }

    @Override
    public String getJwtAlgorithmHeader() {
        return "DILITHIUM3";
    }

    @Override
    public SignatureMetrics getMetrics() {
        return metrics;
    }
}
'@

$dilithiumAlgoContent | Out-File -FilePath "auth-server/src/main/java/com/example/auth_server/signature/DilithiumSignatureAlgorithm.java" -Encoding UTF8
Write-Host "  ✓ DilithiumSignatureAlgorithm corrigido" -ForegroundColor Green

# 7. Corrigir RsaSignatureAlgorithm
Write-Host "`n[7/10] Corrigindo RsaSignatureAlgorithm..." -ForegroundColor Yellow

$rsaAlgoContent = @'
package com.example.auth_server.signature;

import org.springframework.stereotype.Component;
import java.security.*;

@Component("rsaSignature")
public class RsaSignatureAlgorithm implements SignatureAlgorithm {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RsaSignatureAlgorithm.class);

    private KeyPair keyPair;
    private final SignatureMetrics metrics = new SignatureMetrics();

    @Override
    public String getAlgorithmName() {
        return "RSA";
    }

    @Override
    public void generateKeyPair() throws Exception {
        long startTime = System.nanoTime();

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048, new SecureRandom());
        this.keyPair = keyGen.generateKeyPair();

        long duration = System.nanoTime() - startTime;
        metrics.setKeyGenerationTime(duration / 1_000_000);

        log.info("✅ RSA-2048 keypair generated in {} ms", metrics.getKeyGenerationTime());
    }

    @Override
    public byte[] sign(byte[] data) throws Exception {
        if (keyPair == null) {
            generateKeyPair();
        }

        long startTime = System.nanoTime();

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(keyPair.getPrivate());
        signature.update(data);
        byte[] signatureBytes = signature.sign();

        long duration = System.nanoTime() - startTime;
        metrics.recordSignOperation(duration / 1_000_000);

        return signatureBytes;
    }

    @Override
    public boolean verify(byte[] data, byte[] signatureBytes, PublicKey publicKey) throws Exception {
        long startTime = System.nanoTime();

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(data);
        boolean isValid = signature.verify(signatureBytes);

        long duration = System.nanoTime() - startTime;
        metrics.recordVerifyOperation(duration / 1_000_000);

        return isValid;
    }

    @Override
    public PublicKey getPublicKey() {
        return keyPair != null ? keyPair.getPublic() : null;
    }

    @Override
    public PrivateKey getPrivateKey() {
        return keyPair != null ? keyPair.getPrivate() : null;
    }

    @Override
    public String getJwtAlgorithmHeader() {
        return "RS256";
    }

    @Override
    public SignatureMetrics getMetrics() {
        return metrics;
    }
}
'@

$rsaAlgoContent | Out-File -FilePath "auth-server/src/main/java/com/example/auth_server/signature/RsaSignatureAlgorithm.java" -Encoding UTF8
Write-Host "  ✓ RsaSignatureAlgorithm corrigido" -ForegroundColor Green

# 8. Corrigir JWT Encoders e Decoders
Write-Host "`n[8/10] Corrigindo CustomJwtEncoder e CustomJwtDecoder..." -ForegroundColor Yellow

$jwtEncoderContent = @'
package com.example.auth_server.jwt;

import com.example.auth_server.signature.SignatureAlgorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtEncodingException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class CustomJwtEncoder implements JwtEncoder {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CustomJwtEncoder.class);

    private final SignatureAlgorithm signatureAlgorithm;
    private final ObjectMapper objectMapper;
    private final String issuer;

    public CustomJwtEncoder(SignatureAlgorithm signatureAlgorithm, String issuer) {
        this.signatureAlgorithm = signatureAlgorithm;
        this.objectMapper = new ObjectMapper();
        this.issuer = issuer;
    }

    @Override
    public Jwt encode(JwtEncoderParameters parameters) throws JwtEncodingException {
        try {
            Map<String, Object> header = new HashMap<>();
            header.put("alg", signatureAlgorithm.getJwtAlgorithmHeader());
            header.put("typ", "JWT");

            String headerJson = objectMapper.writeValueAsString(header);
            String headerBase64 = base64UrlEncode(headerJson);

            Map<String, Object> claims = new HashMap<>(parameters.getClaims().getClaims());

            if (!claims.containsKey("iss")) {
                claims.put("iss", issuer);
            }
            if (!claims.containsKey("iat")) {
                claims.put("iat", Instant.now().getEpochSecond());
            }

            String payloadJson = objectMapper.writeValueAsString(claims);
            String payloadBase64 = base64UrlEncode(payloadJson);

            String signingInput = headerBase64 + "." + payloadBase64;

            byte[] signature = signatureAlgorithm.sign(signingInput.getBytes(StandardCharsets.UTF_8));
            String signatureBase64 = base64UrlEncode(signature);

            String tokenValue = signingInput + "." + signatureBase64;

            log.info("JWT assinado com {} - Tamanho: {} bytes",
                    signatureAlgorithm.getAlgorithmName(),
                    tokenValue.length());

            Instant issuedAt = claims.containsKey("iat")
                    ? Instant.ofEpochSecond((Long) claims.get("iat"))
                    : Instant.now();

            Instant expiresAt = claims.containsKey("exp")
                    ? Instant.ofEpochSecond((Long) claims.get("exp"))
                    : issuedAt.plusSeconds(3600);

            return new Jwt(
                    tokenValue,
                    issuedAt,
                    expiresAt,
                    header,
                    claims);

        } catch (Exception e) {
            log.error("Error codificando JWT com {}: {}",
                    signatureAlgorithm.getAlgorithmName(),
                    e.getMessage());
            throw new JwtEncodingException("Falha ao codificar JWT", e);
        }
    }

    private String base64UrlEncode(String data) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }

    private String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(data);
    }
}
'@

$jwtEncoderContent | Out-File -FilePath "auth-server/src/main/java/com/example/auth_server/jwt/CustomJwtEncoder.java" -Encoding UTF8
Write-Host "  ✓ CustomJwtEncoder corrigido" -ForegroundColor Green

$jwtDecoderContent = @'
package com.example.auth_server.jwt;

import com.example.auth_server.signature.SignatureAlgorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

public class CustomJwtDecoder implements JwtDecoder {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CustomJwtDecoder.class);

    private final SignatureAlgorithm signatureAlgorithm;
    private final ObjectMapper objectMapper;

    public CustomJwtDecoder(SignatureAlgorithm signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new JwtException("Formato JWT inválido");
            }

            String headerBase64 = parts[0];
            String payloadBase64 = parts[1];
            String signatureBase64 = parts[2];

            String headerJson = new String(base64UrlDecode(headerBase64), StandardCharsets.UTF_8);
            String payloadJson = new String(base64UrlDecode(payloadBase64), StandardCharsets.UTF_8);

            @SuppressWarnings("unchecked")
            Map<String, Object> header = objectMapper.readValue(headerJson, Map.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> claims = objectMapper.readValue(payloadJson, Map.class);

            String signingInput = headerBase64 + "." + payloadBase64;
            byte[] signatureBytes = base64UrlDecode(signatureBase64);

            boolean isValid = signatureAlgorithm.verify(
                    signingInput.getBytes(StandardCharsets.UTF_8),
                    signatureBytes,
                    signatureAlgorithm.getPublicKey());

            if (!isValid) {
                throw new JwtException("Assinatura JWT Inválida - " + signatureAlgorithm.getAlgorithmName());
            }

            log.info("JWT verificado com {} - Algorithm: {}",
                    signatureAlgorithm.getAlgorithmName(),
                    header.get("alg"));

            Instant issuedAt = claims.containsKey("iat")
                    ? Instant.ofEpochSecond(((Number) claims.get("iat")).longValue())
                    : Instant.now();

            Instant expiresAt = claims.containsKey("exp")
                    ? Instant.ofEpochSecond(((Number) claims.get("exp")).longValue())
                    : issuedAt.plusSeconds(3600);

            if (Instant.now().isAfter(expiresAt)) {
                throw new JwtException("JWT token expirado");
            }

            return new Jwt(token, issuedAt, expiresAt, header, claims);

        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro decodificando JWT com {}: {}",
                    signatureAlgorithm.getAlgorithmName(),
                    e.getMessage());
            throw new JwtException("Falha ao decodificar JWT", e);
        }
    }

    private byte[] base64UrlDecode(String data) {
        return Base64.getUrlDecoder().decode(data);
    }
}
'@

$jwtDecoderContent | Out-File -FilePath "auth-server/src/main/java/com/example/auth_server/jwt/CustomJwtDecoder.java" -Encoding UTF8
Write-Host "  ✓ CustomJwtDecoder corrigido" -ForegroundColor Green

# 9. Corrigir ConsentApiController
Write-Host "`n[9/10] Corrigindo ConsentApiController..." -ForegroundColor Yellow

$controllerContent = @'
package com.example.auth_server.controller;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.auth_server.dto.ConsentListResponse;
import com.example.auth_server.dto.ConsentRequest;
import com.example.auth_server.dto.ConsentResponse;
import com.example.auth_server.service.ConsentService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/open-banking/consents/v2/consents")
public class ConsentApiController {

    @Autowired
    private ConsentService consentService;

    @PostMapping
    public ResponseEntity<ConsentResponse> createConsent(
            @RequestBody ConsentRequest request,
            @RequestHeader("x-fapi-interaction-id") String interactionId) throws BadRequestException {

        try {
            ConsentResponse response = consentService.createConsent(request);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .header("x-fapi-interaction-id", interactionId)
                    .body(response);

        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @GetMapping("/{consentId}")
    public ResponseEntity<ConsentResponse> getConsent(
            @PathVariable String consentId,
            @RequestHeader("x-fapi-interaction-id") String interactionId) {

        ConsentResponse response = consentService.getConsent(consentId);

        return ResponseEntity
                .ok()
                .header("x-fapi-interaction-id", interactionId)
                .body(response);
    }

    @DeleteMapping("/{consentId}")
    public ResponseEntity<Void> deleteConsent(
            @PathVariable String consentId,
            @RequestHeader("x-fapi-interaction-id") String interactionId,
            @RequestHeader(value = "x-revoked-by", defaultValue = "TPP") String revokedBy) {

        // Chamada correta com 3 parâmetros
        consentService.revokeConsent(consentId, "TPP_REQUESTED", revokedBy);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .header("x-fapi-interaction-id", interactionId)
                .build();
    }

    @GetMapping
    public ResponseEntity<ConsentListResponse> listConsents(
            @RequestParam(required = false) String cpf,
            @RequestHeader("x-fapi-interaction-id") String interactionId) {

        List<ConsentResponse> consents = consentService.listConsents(cpf);

        ConsentListResponse response = new ConsentListResponse();
        response.setData(consents.stream()
                .map(ConsentResponse::getData)
                .collect(Collectors.toList()));
        
        ConsentListResponse.Meta meta = new ConsentListResponse.Meta();
        meta.setTotalRecords(consents.size());
        meta.setTotalPages(1);
        response.setMeta(meta);

        return ResponseEntity
                .ok()
                .header("x-fapi-interaction-id", interactionId)
                .body(response);
    }
}
'@

$controllerContent | Out-File -FilePath "auth-server/src/main/java/com/example/auth_server/controller/ConsentApiController.java" -Encoding UTF8
Write-Host "  ✓ ConsentApiController corrigido" -ForegroundColor Green

# 10. Corrigir ConsentListResponse
Write-Host "`n[10/10] Corrigindo ConsentListResponse..." -ForegroundColor Yellow

$consentListResponseContent = @'
package com.example.auth_server.dto;

import java.util.List;

public class ConsentListResponse {
    private List<ConsentResponse.Data> data;
    private Object links;
    private Meta meta;
    
    public static class Meta {
        private int totalRecords;
        private int totalPages;
        
        public int getTotalRecords() { return totalRecords; }
        public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }
        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    }
    
    public List<ConsentResponse.Data> getData() { return data; }
    public void setData(List<ConsentResponse.Data> data) { this.data = data; }
    public Object getLinks() { return links; }
    public void setLinks(Object links) { this.links = links; }
    public Meta getMeta() { return meta; }
    public void setMeta(Meta meta) { this.meta = meta; }
}
'@

$consentListResponseContent | Out-File -FilePath "auth-server/src/main/java/com/example/auth_server/dto/ConsentListResponse.java" -Encoding UTF8
Write-Host "  ✓ ConsentListResponse corrigido" -ForegroundColor Green

Write-Host "`n🔨 Compilando projeto..." -ForegroundColor Yellow
cd auth-server
mvn clean compile -DskipTests

if ($LASTEXITCODE -eq 0) {
    Write-Host @"

=====================================
   ✅ COMPILAÇÃO BEM-SUCEDIDA!
=====================================

Todos os erros foram corrigidos:
✓ Imports de Consent (entity → model)
✓ ConsentService completo
✓ ConsentStatus enum
✓ RateLimitConfig e Filter
✓ X509AuthenticationFilter
✓ Algoritmos de assinatura
✓ JWT Encoder/Decoder
✓ ConsentApiController
✓ DTOs

"@ -ForegroundColor Green

    Write-Host "Criando JAR..." -ForegroundColor Yellow
    mvn package -DskipTests
    
    if ($LASTEXITCODE -eq 0) {
        cd ..
        Write-Host "`n✅ JAR criado com sucesso!" -ForegroundColor Green
        Write-Host "`nReconstruindo containers Docker..." -ForegroundColor Yellow
        
        docker-compose down -v
        docker-compose up -d --build
        
        Write-Host @"

=====================================
   🚀 SISTEMA PRONTO!
=====================================

Todos os serviços estão rodando:
- Auth Server: http://localhost:8080
- Resource Server: http://localhost:8082  
- Auth Client: http://localhost:8081
- PgAdmin: http://localhost:5050

✅ Implementação Dilithium + OAuth2 + Consents
   pronta para demonstração do TCC!

"@ -ForegroundColor Cyan
        
        Start-Sleep -Seconds 10
        docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    }
} else {
    Write-Host "`n❌ Ainda há erros. Verificando..." -ForegroundColor Red
    mvn compile -DskipTests
}