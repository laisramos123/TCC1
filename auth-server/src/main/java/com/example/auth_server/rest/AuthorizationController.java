package com.example.auth_server.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.auth_server.signer.*;;

@RestController
public class AuthorizationController {

    private final IdTokenSigner idTokenSigner;

    public AuthorizationController(IdTokenSigner idTokenSigner) {
        this.idTokenSigner = idTokenSigner;
    }

    @GetMapping("/authorize")
    public ResponseEntity<String> authorize(@RequestParam String issuer, @RequestParam String subject, @RequestParam String audience) throws Exception {
        return ResponseEntity.ok(idTokenSigner.generateSignedIdToken(issuer, subject, audience));
    }
}