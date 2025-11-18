
package com.example.resource_server;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class ResourceServerApplication {
    static {
        Security.addProvider(new BouncyCastleProvider());
        Security.addProvider(new BouncyCastlePQCProvider());
    }

    public static void main(String[] args) {
        SpringApplication.run(ResourceServerApplication.class, args);
    }

    @PostConstruct
    public void verifyProviders() {
        if (Security.getProvider("BC") == null) {
            throw new RuntimeException("BouncyCastle provider não carregado!");
        }
        if (Security.getProvider("BCPQC") == null) {
            throw new RuntimeException("BouncyCastle PQC provider não carregado!");
        }
        System.out.println(" Providers  carregados com sucesso!");
    }
}