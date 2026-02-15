package com.tradebridge.backend.auth.model;

import com.tradebridge.backend.common.UserRole;

public record TokenResponse(String accessToken, String refreshToken, String userId, String companyId, UserRole role) {
}
