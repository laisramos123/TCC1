package com.example.auth_server.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String home(Principal principal) {
        if (principal != null) {
            return "dashboard";
        }
        return "redirect:/login";
    }
}
