package com.tradebridge.backend.auth;

import com.tradebridge.backend.common.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterCompanyRequest(
        @NotBlank String companyName,
        @Email @NotBlank String email,
        @NotBlank String password,
        @NotNull UserRole role) {
}
