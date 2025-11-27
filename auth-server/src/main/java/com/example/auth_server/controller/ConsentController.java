package com.example.auth_server.controller;

import com.example.auth_server.service.ConsentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/consent")
public class ConsentController {

    @Autowired
    private ConsentService consentService;

    @GetMapping("/{consentId}")
    public String showConsentPage(@PathVariable String consentId, Model model) {
        model.addAttribute("consentId", consentId);
        return "consent";
    }

    @PostMapping("/{consentId}/authorize")
    public String authorizeConsent(@PathVariable String consentId) {

        return "redirect:/consent/" + consentId + "/success";
    }
}
