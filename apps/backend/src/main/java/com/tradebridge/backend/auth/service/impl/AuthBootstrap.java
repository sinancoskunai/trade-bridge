package com.tradebridge.backend.auth;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class AuthBootstrap {

    private final AuthApplicationService authService;

    public AuthBootstrap(AuthApplicationService authService) {
        this.authService = authService;
    }

    @PostConstruct
    public void ensureAdminUser() {
        authService.ensurePlatformAdminUser();
    }
}
