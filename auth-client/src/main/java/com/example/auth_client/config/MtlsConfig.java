package com.example.auth_client.config;

import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class MtlsConfig {
    private static final Logger logger = LoggerFactory.getLogger(MtlsConfig.class);

    @Value("${open-finance.mtls.enabled:false}")
    private boolean mtlsEnabled;

    @Value("${open-finance.mtls.keystore-path:certificates/tpp-client-keystore.p12}")
    private String keystorePath;

    @Value("${open-finance.mtls.keystore-password:tcc-openFinance}")
    private String keystorePassword;

    @Value("${open-finance.mtls.truststore-path:certificates/truststore.p12}")
    private String truststorePath;

    @Value("${open-finance.mtls.truststore-password:tcc-openFinance}")
    private String truststorePassword;

    @Bean
    public RestTemplate mtlsRestTemplate() {
        if (!mtlsEnabled) {
            logger.warn("========================================");
            logger.warn("⚠️  mTLS DESABILITADO");
            logger.warn("========================================");
            return new RestTemplate();
        }

        try {
            logger.info("🔐 Configurando mTLS...");
            logger.info("   Keystore: {}", keystorePath);
            logger.info("   Truststore: {}", truststorePath);

            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            try (InputStream is = new ClassPathResource(keystorePath).getInputStream()) {
                keyStore.load(is, keystorePassword.toCharArray());
            }

            KeyStore trustStore = KeyStore.getInstance("PKCS12");
            try (InputStream is = new ClassPathResource(truststorePath).getInputStream()) {
                trustStore.load(is, truststorePassword.toCharArray());
            }

            SSLContext sslContext = SSLContextBuilder.create()
                    .loadKeyMaterial(keyStore, keystorePassword.toCharArray())
                    .loadTrustMaterial(trustStore, null)
                    .build();

            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);

            HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                    .setSSLSocketFactory(socketFactory)
                    .build();

            CloseableHttpClient httpClient = HttpClients.custom()
                    .setConnectionManager(connectionManager)
                    .build();

            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
            factory.setConnectTimeout(10000);

            logger.info("✅ mTLS configurado com sucesso!");
            return new RestTemplate(factory);

        } catch (Exception e) {
            logger.error("❌ Erro configurando mTLS: {}", e.getMessage(), e);
            logger.warn("⚠️  Retornando RestTemplate sem mTLS");
            return new RestTemplate();
        }
    }
}