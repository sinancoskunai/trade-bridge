package com.tradebridge.backend.auth.service.impl;

import org.springframework.stereotype.Component;

import com.tradebridge.backend.auth.service.AuthApplicationService;

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
