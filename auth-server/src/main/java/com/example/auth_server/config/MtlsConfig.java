package com.example.auth_server.config;

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

import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.security.KeyStore;

@Configuration
public class MtlsConfig {

    private static final Logger logger = LoggerFactory.getLogger(MtlsConfig.class);

    @Value("${open-finance.mtls.enabled:false}")
    private boolean mtlsEnabled;

    @Value("${open-finance.mtls.keystore-path:certificates/auth-server-keystore.p12}")
    private String keystorePath;

    @Value("${open-finance.mtls.keystore-password:changeit}")
    private String keystorePassword;

    @Value("${open-finance.mtls.truststore-path:certificates/truststore.p12}")
    private String truststorePath;

    @Value("${open-finance.mtls.truststore-password:changeit}")
    private String truststorePassword;

    @Bean
    public RestTemplate mtlsRestTemplate() {
        if (!mtlsEnabled) {
            logger.warn("========================================");
            logger.warn("   mTLS DESABILITADO");
            logger.warn("   Defina open-finance.mtls.enabled=true para habilitar");
            logger.warn("========================================");
            return new RestTemplate();
        }

        try {
            logger.info("   Configurando mTLS para auth-server...");

            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            try (InputStream is = new ClassPathResource(keystorePath).getInputStream()) {
                keyStore.load(is, keystorePassword.toCharArray());
            }
            logger.info("   Keystore carregado: {}", keystorePath);

            KeyStore trustStore = KeyStore.getInstance("PKCS12");
            try (InputStream is = new ClassPathResource(truststorePath).getInputStream()) {
                trustStore.load(is, truststorePassword.toCharArray());
            }
            logger.info("   Truststore carregado: {}", truststorePath);

            SSLContext sslContext = SSLContextBuilder.create()
                    .loadKeyMaterial(keyStore, keystorePassword.toCharArray())
                    .loadTrustMaterial(trustStore, null)
                    .build();

            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext);

            HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                    .setSSLSocketFactory(sslSocketFactory)
                    .build();

            CloseableHttpClient httpClient = HttpClients.custom()
                    .setConnectionManager(connectionManager)
                    .build();

            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(
                    httpClient);
            requestFactory.setConnectTimeout(10000);

            logger.info("   mTLS configurado com sucesso para auth-server!");
            return new RestTemplate(requestFactory);

        } catch (Exception e) {
            logger.error("   Erro configurando mTLS: {}", e.getMessage(), e);
            logger.warn("   Retornando RestTemplate sem mTLS");
            return new RestTemplate();
        }
    }
}