package com.example.auth_server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.x509.SubjectDnX509PrincipalExtractor;
import org.springframework.security.web.authentication.preauth.x509.X509AuthenticationFilter;
import org.springframework.security.web.authentication.preauth.x509.X509PrincipalExtractor;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.KeyManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.List;

/**
 * Configuração mTLS para Open Finance Brasil
 */
@Configuration
public class MtlsConfig {

    @Value("${open-finance.mtls.enabled:true}")
    private boolean mtlsEnabled;

    @Value("${server.ssl.trust-store}")
    private String trustStorePath;

    @Value("${server.ssl.trust-store-password}")
    private String trustStorePassword;

    /**
     * Filtro de autenticação X.509
     */
    @Bean
    public X509AuthenticationFilter x509AuthenticationFilter() {
        X509AuthenticationFilter filter = new X509AuthenticationFilter();
        filter.setPrincipalExtractor(principalExtractor());
        return filter;
    }

    /**
     * Extrator de principal do certificado
     * Extrai o CN (Common Name) do certificado
     */
    @Bean
    public X509PrincipalExtractor principalExtractor() {
        SubjectDnX509PrincipalExtractor extractor = new SubjectDnX509PrincipalExtractor();
        extractor.setSubjectDnRegex("CN=(.*?)(?:,|$)");
        return extractor;
    }

    /**
     * Configuração SSL Context customizado
     */
    @Bean
    public SSLContext sslContext() throws Exception {

        // Carrega TrustStore
        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(trustStorePath.replace("classpath:", "src/main/resources/"))) {
            trustStore.load(fis, trustStorePassword.toCharArray());
        }

        // Configura TrustManager
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        // Cria SSL Context
        SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
        sslContext.init(null, tmf.getTrustManagers(), null);

        return sslContext;
    }
}