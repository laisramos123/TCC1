package com.example.auth_server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Registrar o interceptor
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final OAuth2SessionInterceptor oauth2SessionInterceptor;

    public WebMvcConfig(OAuth2SessionInterceptor oauth2SessionInterceptor) {
        this.oauth2SessionInterceptor = oauth2SessionInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(oauth2SessionInterceptor)
                .addPathPatterns("/oauth2/**");
    }
}
