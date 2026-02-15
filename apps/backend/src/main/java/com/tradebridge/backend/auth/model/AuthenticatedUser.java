package com.tradebridge.backend.auth.model;

import com.tradebridge.backend.common.UserRole;

public record AuthenticatedUser(String userId, String companyId, String email, UserRole role, boolean companyApproved) {
}
