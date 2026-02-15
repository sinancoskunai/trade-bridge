package com.tradebridge.backend.auth;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class AuthBootstrap {

    private final AuthService authService;

    public AuthBootstrap(AuthService authService) {
        this.authService = authService;
    }

    @PostConstruct
    public void ensureAdminUser() {
        authService.ensurePlatformAdminUser();
    }
}
