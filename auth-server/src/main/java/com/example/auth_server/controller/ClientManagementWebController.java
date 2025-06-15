package com.example.auth_server.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;

public class ClientManagementWebController {
    @GetMapping("/admin/clients")
    @PreAuthorize("hasRole('ADMIN')")
    public String clientManagementPage() {
        return "client-management";
    }
}
