package com.tradebridge.backend.auth;

public interface AuthApplicationService {

    CompanyApprovalResponse registerCompany(RegisterCompanyRequest request);

    CompanyApprovalResponse approveCompany(String companyId, AuthenticatedUser adminUser);

    TokenResponse login(LoginRequest request);

    TokenResponse refresh(RefreshRequest request);

    UserProfileResponse profile(AuthenticatedUser user);

    void ensurePlatformAdminUser();
}
