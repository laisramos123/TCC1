package com.example.auth_server.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2SessionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // ✅ APENAS LOG - NÃO MODIFICAR SESSÃO PARA NÃO INTERFERIR NO FLUXO
        if (path.contains("oauth2")) {
            System.out.println("🔍 OAuth2 Request: " + method + " " + path);

            // ✅ LOG SEGURO - Verificar nulls antes de logar
            String clientId = request.getParameter("client_id");
            String state = request.getParameter("state");
            String scope = request.getParameter("scope");
            String redirectUri = request.getParameter("redirect_uri");
            String responseType = request.getParameter("response_type");
            String nonce = request.getParameter("nonce");

            if (clientId != null) {
                System.out.println("   - Client ID: " + clientId);
            }
            if (state != null) {
                System.out.println("   - State: " + state);
            }
            if (scope != null) {
                System.out.println("   - Scope: " + scope);
            }
            if (redirectUri != null) {
                System.out.println("   - Redirect URI: " + redirectUri);
            }
            if (responseType != null) {
                System.out.println("   - Response Type: " + responseType);
            }
            if (nonce != null) {
                System.out.println("   - Nonce: " + nonce);
            }

            System.out.println(" OAuth2 Request processado com sucesso");
        }

        return true; // ✅ Sempre continuar - não interferir no fluxo
    }
}