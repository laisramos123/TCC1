package com.example.resource_server.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.resource_server.dto.AccountDTO;
import com.example.resource_server.dto.UserDTO;
import com.example.resource_server.service.AccountService;
import com.example.resource_server.service.UserService;

@RestController
@RequestMapping("/api")
public class UserResourceController {
    private final UserService userService;
    private final AccountService accountService;

    @Autowired
    public UserResourceController(UserService userService,
            AccountService accountService) {
        this.userService = userService;
        this.accountService = accountService;
    }

    @GetMapping("/me")
    public UserDTO getUserInfo(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return userService.findUserById(userId);
    }

    @GetMapping("/accounts")
    public List<AccountDTO> getAccounts(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return accountService.findAccountsByUserId(userId);
    }

    @GetMapping("/accounts/{accountId}")
    public AccountDTO getAccount(@PathVariable String accountId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();

        if (!accountService.hasAccessToAccount(userId, accountId)) {
            throw new AccessDeniedException("User does not have access to this account");
        }

        return accountService.findAccountById(accountId);
    }

}
