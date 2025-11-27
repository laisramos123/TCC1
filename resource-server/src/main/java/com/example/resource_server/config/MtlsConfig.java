// package com.example.resource_server.config;

// import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
// import org.apache.hc.client5.http.impl.classic.HttpClients;
// import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
// import
// org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
// import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
// import org.apache.hc.core5.ssl.SSLContextBuilder;
// import org.apache.hc.core5.ssl.TrustStrategy;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import
// org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
// import
// org.springframework.security.web.authentication.preauth.x509.SubjectDnX509PrincipalExtractor;
// import
// org.springframework.security.web.authentication.preauth.x509.X509PrincipalExtractor;
// import org.springframework.web.client.RestTemplate;

// import javax.net.ssl.SSLContext;
// import java.io.FileInputStream;
// import java.security.KeyStore;
// import java.security.cert.X509Certificate;

// @Configuration
// public class MtlsConfig {

// @Value("${server.ssl.key-store}")
// private String keyStorePath;

// @Value("${server.ssl.key-store-password}")
// private String keyStorePassword;

// @Value("${server.ssl.trust-store}")
// private String trustStorePath;

// @Value("${server.ssl.trust-store-password}")
// private String trustStorePassword;

// @Bean
// public RestTemplate mtlsRestTemplate() throws Exception {

// KeyStore keyStore = KeyStore.getInstance("PKCS12");
// try (FileInputStream kis = new
// FileInputStream(resolveClasspath(keyStorePath))) {
// keyStore.load(kis, keyStorePassword.toCharArray());
// }

// KeyStore trustStore = KeyStore.getInstance("PKCS12");
// try (FileInputStream tis = new
// FileInputStream(resolveClasspath(trustStorePath))) {
// trustStore.load(tis, trustStorePassword.toCharArray());
// }

// SSLContext sslContext = SSLContextBuilder.create()
// .loadKeyMaterial(keyStore, keyStorePassword.toCharArray())
// .loadTrustMaterial(trustStore, new TrustStrategy() {
// @Override
// public boolean isTrusted(X509Certificate[] chain, String authType) {
// return true;
// }
// })
// .build();

// SSLConnectionSocketFactory socketFactory = new
// SSLConnectionSocketFactory(sslContext);

// PoolingHttpClientConnectionManager connectionManager =
// PoolingHttpClientConnectionManagerBuilder.create()
// .setSSLSocketFactory(socketFactory)
// .build();

// CloseableHttpClient httpClient = HttpClients.custom()
// .setConnectionManager(connectionManager)
// .build();

// HttpComponentsClientHttpRequestFactory factory = new
// HttpComponentsClientHttpRequestFactory();
// factory.setHttpClient(httpClient);

// return new RestTemplate(factory);
// }

// @Bean
// public X509PrincipalExtractor principalExtractor() {
// SubjectDnX509PrincipalExtractor extractor = new
// SubjectDnX509PrincipalExtractor();
// extractor.setSubjectDnRegex("CN=(.*?)(?:,|$)");
// return extractor;
// }

// private String resolveClasspath(String path) {
// if (path.startsWith("classpath:")) {
// return "src/main/resources/" + path.substring(10);
// }
// return path;
// }
// }
package com.example.resource_server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class MtlsConfig {
    private static final Logger logger = LoggerFactory.getLogger(MtlsConfig.class);

    @Value("${open-finance.mtls.enabled:false}")
    private boolean mtlsEnabled;

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

            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            try (InputStream is = new ClassPathResource("certificates/tpp-client-keystore.p12").getInputStream()) {
                keyStore.load(is, "changeit".toCharArray());
            }

            KeyStore trustStore = KeyStore.getInstance("PKCS12");
            try (InputStream is = new ClassPathResource("certificates/truststore.p12").getInputStream()) {
                trustStore.load(is, "changeit".toCharArray());
            }

            SSLContext sslContext = SSLContextBuilder.create()
                    .loadKeyMaterial(keyStore, "changeit".toCharArray())
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

            logger.info("  mTLS configurado com sucesso!");
            return new RestTemplate(factory);

        } catch (Exception e) {
            logger.error("  Erro configurando mTLS: {}", e.getMessage(), e);
            logger.warn("  Retornando RestTemplate sem mTLS");
            return new RestTemplate();
        }
    }
}