// auth-server/src/main/java/com/example/auth_server/config/MtlsConfig.java
package com.example.auth_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.security.KeyStore;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.KeyManagerFactory;

@Configuration
public class MtlsConfig {

    private static final Logger logger = LoggerFactory.getLogger(MtlsConfig.class);

    @Bean
    public RestTemplate mtlsRestTemplate(RestTemplateBuilder builder) {
        try {
            logger.info(" Configurando mTLS para Open Finance Brasil...");

            ClassPathResource keystoreResource = new ClassPathResource("certificates/auth-server-keystore.p12");
            ClassPathResource truststoreResource = new ClassPathResource("certificates/auth-server-truststore.p12");

            if (!keystoreResource.exists() || !truststoreResource.exists()) {
                logger.warn("  Certificados mTLS não encontrados!");
                logger.warn("  Open Finance REQUER mTLS em produção!");
                return builder.build();
            }

            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            try (InputStream is = keystoreResource.getInputStream()) {
                if (is.available() < 100) {
                    logger.warn("  Keystore parece estar vazio ou corrompido");
                    return builder.build();
                }
                keyStore.load(is, "openfinance".toCharArray());
                logger.info("  Keystore carregado");
            }

            KeyStore trustStore = KeyStore.getInstance("PKCS12");
            try (InputStream is = truststoreResource.getInputStream()) {
                if (is.available() < 100) {
                    logger.warn("  Truststore parece estar vazio ou corrompido");
                    return builder.build();
                }
                trustStore.load(is, "openfinance".toCharArray());
                logger.info("  Truststore carregado");
            }

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keyStore, "openfinance".toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            RestTemplate restTemplate = builder
                    .setConnectTimeout(java.time.Duration.ofSeconds(10))
                    .setReadTimeout(java.time.Duration.ofSeconds(10))
                    .build();

            logger.info("  mTLS configurado para Open Finance Brasil!");
            return restTemplate;

        } catch (Exception e) {
            logger.error("  Erro configurando mTLS: {}", e.getMessage());
            logger.warn("⚠️ ATENÇÃO: Open Finance requer mTLS!");
            return builder.build();
        }
    }
}