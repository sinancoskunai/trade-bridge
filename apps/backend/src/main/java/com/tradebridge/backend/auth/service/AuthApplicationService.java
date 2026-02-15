package com.tradebridge.backend.auth.service;

import com.tradebridge.backend.auth.model.AuthenticatedUser;
import com.tradebridge.backend.auth.model.CompanyApprovalResponse;
import com.tradebridge.backend.auth.model.LoginRequest;
import com.tradebridge.backend.auth.model.RefreshRequest;
import com.tradebridge.backend.auth.model.RegisterCompanyRequest;
import com.tradebridge.backend.auth.model.TokenResponse;
import com.tradebridge.backend.auth.model.UserProfileResponse;

public interface AuthApplicationService {

    CompanyApprovalResponse registerCompany(RegisterCompanyRequest request);

    CompanyApprovalResponse approveCompany(String companyId, AuthenticatedUser adminUser);

    TokenResponse login(LoginRequest request);

    TokenResponse refresh(RefreshRequest request);

    UserProfileResponse profile(AuthenticatedUser user);

    void ensurePlatformAdminUser();
}
