package com.tradebridge.backend.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradebridge.backend.auth.model.CompanyApprovalResponse;
import com.tradebridge.backend.auth.model.LoginRequest;
import com.tradebridge.backend.auth.model.RefreshRequest;
import com.tradebridge.backend.auth.model.RegisterCompanyRequest;
import com.tradebridge.backend.auth.model.TokenResponse;
import com.tradebridge.backend.auth.model.UserProfileResponse;
import com.tradebridge.backend.auth.service.AuthApplicationService;
import com.tradebridge.backend.auth.service.impl.SecurityUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping
public class AuthController {

    private final AuthApplicationService authService;

    public AuthController(AuthApplicationService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/register-company")
    public CompanyApprovalResponse registerCompany(@Valid @RequestBody RegisterCompanyRequest request) {
        return authService.registerCompany(request);
    }

    @PostMapping("/auth/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/auth/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request);
    }

    @GetMapping("/users/me")
    public UserProfileResponse me() {
        return authService.profile(SecurityUtil.currentUser());
    }

    @PostMapping("/admin/companies/{companyId}/approve")
    public CompanyApprovalResponse approve(@PathVariable String companyId) {
        return authService.approveCompany(companyId, SecurityUtil.currentUser());
    }
}
