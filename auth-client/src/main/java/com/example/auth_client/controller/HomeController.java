package com.example.auth_client.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String dashboard() {
        return "quantumBank";
    }

    @GetMapping("/open-finance")
    public String openFinance() {
        return "home";
    }

    @GetMapping("/error")
    public String handleError() {
        return "error";
    }
}
