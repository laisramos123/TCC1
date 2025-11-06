package com.example.auth_client.config;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

/**
 * Configuração mTLS para TPP Client usando HttpClient 5
 */
@Configuration
public class MtlsConfig {

    @Value("${open-finance.mtls.client-certificate-path:classpath:certificates/tpp-client-keystore.p12}")
    private String clientCertPath;

    @Value("${open-finance.mtls.client-certificate-password:changeit}")
    private String clientCertPassword;

    @Value("${server.ssl.trust-store:classpath:certificates/truststore.p12}")
    private String trustStorePath;

    @Value("${server.ssl.trust-store-password:changeit}")
    private String trustStorePassword;

    /**
     * RestTemplate configurado com mTLS usando HttpClient 5
     */
    @Bean
    public RestTemplate mtlsRestTemplate() throws Exception {

        // Carrega certificado do cliente (KeyStore)
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        String keyStoreFile = resolveClasspath(clientCertPath);
        try (FileInputStream kis = new FileInputStream(keyStoreFile)) {
            keyStore.load(kis, clientCertPassword.toCharArray());
        }

        // Carrega certificados confiáveis (TrustStore)
        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        String trustStoreFile = resolveClasspath(trustStorePath);
        try (FileInputStream tis = new FileInputStream(trustStoreFile)) {
            trustStore.load(tis, trustStorePassword.toCharArray());
        }

        // Configura SSL Context com mTLS
        SSLContext sslContext = SSLContextBuilder.create()
                .loadKeyMaterial(keyStore, clientCertPassword.toCharArray())
                .loadTrustMaterial(trustStore, new TrustStrategy() {
                    @Override
                    public boolean isTrusted(X509Certificate[] chain, String authType) {
                        return true; // Para DEV - validar certificados em produção
                    }
                })
                .build();

        // Configura Socket Factory
        // ATENÇÃO: NoopHostnameVerifier apenas para DEV!
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
                sslContext,
                NoopHostnameVerifier.INSTANCE // REMOVER EM PRODUÇÃO
        );

        // Configura Connection Manager
        HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(socketFactory)
                .build();

        // Cria HttpClient 5
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();

        // Configura RestTemplate
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(10000); // 10 segundos

        return new RestTemplate(factory);
    }

    /**
     * RestTemplate padrão (sobrescreve o bean original)
     */
    @Bean
    public RestTemplate restTemplate() throws Exception {
        return mtlsRestTemplate();
    }

    /**
     * Resolve classpath para caminho absoluto
     */
    private String resolveClasspath(String path) {
        if (path.startsWith("classpath:")) {
            return "src/main/resources/" + path.substring(10);
        }
        return path;
    }
}