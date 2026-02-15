package com.tradebridge.backend.auth;

import com.tradebridge.backend.common.UserRole;

public record UserProfileResponse(String userId, String companyId, String email, UserRole role, boolean companyApproved) {
}
