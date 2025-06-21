package com.example.auth_server.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class OAuth2SessionInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        String path = request.getRequestURI();

        if (path.equals("/oauth2/consent")) {
            System.out.println("🔍 OAuth2 Interceptor - Path: " + path);

            // Para requisições GET, preservar dados existentes
            if ("GET".equals(request.getMethod())) {
                preservarParametrosSeNecessario(request);
            }
            // Para POST, NÃO modificar a sessão

            debugSessionState(request);
        }

        return true;
    }

    private void preservarParametrosSeNecessario(HttpServletRequest request) {
        HttpSession session = request.getSession();

        // Só adicionar parâmetros se NÃO existirem na sessão
        adicionarSeAusente(session, "oauth2_client_id", request.getParameter("client_id"));
        adicionarSeAusente(session, "oauth2_scope", request.getParameter("scope"));
        adicionarSeAusente(session, "oauth2_state", request.getParameter("state"));
        adicionarSeAusente(session, "oauth2_redirect_uri", request.getParameter("redirect_uri"));
        adicionarSeAusente(session, "oauth2_response_type", request.getParameter("response_type"));
        adicionarSeAusente(session, "oauth2_nonce", request.getParameter("nonce"));
    }

    private void adicionarSeAusente(HttpSession session, String chave, String valor) {
        if (valor != null && session.getAttribute(chave) == null) {
            session.setAttribute(chave, valor);
            System.out.println("💾 Interceptor salvando: " + chave + " = " + valor);
        } else if (session.getAttribute(chave) != null) {
            System.out.println("⏭️ Interceptor preservando: " + chave + " = " + session.getAttribute(chave));
        }
    }

    private void debugSessionState(HttpServletRequest request) {
        HttpSession session = request.getSession();

        System.out.println("📊 Estado da sessão após interceptor:");
        String[] chaves = { "oauth2_client_id", "oauth2_redirect_uri", "oauth2_response_type",
                "oauth2_scope", "oauth2_state", "oauth2_nonce" };

        for (String chave : chaves) {
            Object valor = session.getAttribute(chave);
            if (valor != null) {
                System.out.println("  - " + chave + " = " + valor);
            }
        }
    }
}
