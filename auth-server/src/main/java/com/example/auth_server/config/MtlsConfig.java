package com.example.auth_server.config;

// import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
// import org.apache.hc.client5.http.impl.classic.HttpClients;
// import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
// import org.apache.hc.client5.http.io.HttpClientConnectionManager;
// import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
// import org.apache.hc.core5.ssl.SSLContexts;
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

// @Configuration
// public class MtlsConfig {

//     private static final Logger logger = LoggerFactory.getLogger(MtlsConfig.class);

//     @Bean
//     public RestTemplate mtlsRestTemplate() {
//         try {
//             logger.info("🔐 Configurando mTLS para Open Finance Brasil...");

//             // Carregar Keystore
//             KeyStore keyStore = KeyStore.getInstance("PKCS12");
//             try (InputStream is = new ClassPathResource("certificates/auth-server-keystore.p12").getInputStream()) {
//                 keyStore.load(is, "changeit".toCharArray());
//             }
//             logger.info("✅ Keystore carregado");

//             // Carregar Truststore
//             KeyStore trustStore = KeyStore.getInstance("PKCS12");
//             try (InputStream is = new ClassPathResource("certificates/auth-server-truststore.p12").getInputStream()) {
//                 trustStore.load(is, "changeit".toCharArray());
//             }
//             logger.info("✅ Truststore carregado");

//             // Criar SSLContext
//             SSLContext sslContext = SSLContexts.custom()
//                     .loadKeyMaterial(keyStore, "changeit".toCharArray())
//                     .loadTrustMaterial(trustStore, null)
//                     .build();

//             // Criar SSLConnectionSocketFactory
//             SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext);

//             // Criar HttpClientConnectionManager
//             HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
//                     .setSSLSocketFactory(sslSocketFactory)
//                     .build();

//             // Criar HttpClient
//             CloseableHttpClient httpClient = HttpClients.custom()
//                     .setConnectionManager(connectionManager)
//                     .build();

//             // Criar RequestFactory
//             HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(
//                     httpClient);
//             requestFactory.setConnectTimeout(10000);

//             // Criar RestTemplate com SSL
//             RestTemplate restTemplate = new RestTemplate(requestFactory);

//             logger.info("✅ mTLS configurado com sucesso!");
//             return restTemplate;

//         } catch (Exception e) {
//             logger.error("❌ Erro configurando mTLS: {}", e.getMessage(), e);
//             logger.warn("⚠️ Retornando RestTemplate sem mTLS");
//             return new RestTemplate();
//         }
//     }
// }
@Configuration
public class MtlsConfig {

    private static final Logger logger = LoggerFactory.getLogger(MtlsConfig.class);

    @Bean
    public RestTemplate mtlsRestTemplate() {
        logger.warn("========================================");
        logger.warn("⚠️  mTLS DESABILITADO");
        logger.warn("⚠️  Configuração temporária para desenvolvimento");
        logger.warn("⚠️  NÃO use em produção!");
        logger.warn("========================================");

        return new RestTemplate();
    }
}