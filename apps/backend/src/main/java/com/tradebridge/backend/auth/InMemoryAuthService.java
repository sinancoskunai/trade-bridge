package com.tradebridge.backend.auth;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.tradebridge.backend.common.ApiException;
import com.tradebridge.backend.common.UserRole;

@Service
public class InMemoryAuthService {

    private final Map<String, CompanyRecord> companiesById = new ConcurrentHashMap<>();
    private final Map<String, UserRecord> usersByEmail = new ConcurrentHashMap<>();
    private final Map<String, AuthenticatedUser> accessTokens = new ConcurrentHashMap<>();
    private final Map<String, AuthenticatedUser> refreshTokens = new ConcurrentHashMap<>();

    public InMemoryAuthService() {
        String companyId = "platform";
        String userId = UUID.randomUUID().toString();
        companiesById.put(companyId, new CompanyRecord(companyId, "Trade Bridge Platform", true));
        usersByEmail.put("admin@tradebridge.local",
                new UserRecord(userId, companyId, "admin@tradebridge.local", "admin123", UserRole.ADMIN));
    }

    public CompanyApprovalResponse registerCompany(RegisterCompanyRequest request) {
        if (request.role() == UserRole.ADMIN) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ADMIN role cannot self-register");
        }
        if (usersByEmail.containsKey(request.email().toLowerCase())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already in use");
        }

        String companyId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        companiesById.put(companyId, new CompanyRecord(companyId, request.companyName(), false));
        usersByEmail.put(request.email().toLowerCase(),
                new UserRecord(userId, companyId, request.email().toLowerCase(), request.password(), request.role()));

        return new CompanyApprovalResponse(companyId, false);
    }

    public CompanyApprovalResponse approveCompany(String companyId) {
        CompanyRecord company = companiesById.get(companyId);
        if (company == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Company not found");
        }
        company.setApproved(true);
        return new CompanyApprovalResponse(company.id(), true);
    }

    public TokenResponse login(LoginRequest request) {
        UserRecord user = usersByEmail.get(request.email().toLowerCase());
        if (user == null || !user.password().equals(request.password())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        CompanyRecord company = companiesById.get(user.companyId());
        if (user.role() != UserRole.ADMIN && !company.approved()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Company is not approved yet");
        }

        AuthenticatedUser authUser = new AuthenticatedUser(user.id(), user.companyId(), user.email(), user.role(),
                company.approved());
        String accessToken = "at_" + UUID.randomUUID();
        String refreshToken = "rt_" + UUID.randomUUID();
        accessTokens.put(accessToken, authUser);
        refreshTokens.put(refreshToken, authUser);

        return new TokenResponse(accessToken, refreshToken, user.id(), user.companyId(), user.role());
    }

    public TokenResponse refresh(RefreshRequest request) {
        AuthenticatedUser user = refreshTokens.get(request.refreshToken());
        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }
        String accessToken = "at_" + UUID.randomUUID();
        String refreshToken = "rt_" + UUID.randomUUID();
        accessTokens.put(accessToken, user);
        refreshTokens.put(refreshToken, user);
        return new TokenResponse(accessToken, refreshToken, user.userId(), user.companyId(), user.role());
    }

    public UserProfileResponse profile(AuthenticatedUser user) {
        return new UserProfileResponse(user.userId(), user.companyId(), user.email(), user.role(), user.companyApproved());
    }

    public AuthenticatedUser byAccessToken(String token) {
        return accessTokens.get(token);
    }

    private record UserRecord(String id, String companyId, String email, String password, UserRole role) {
    }

    private static final class CompanyRecord {
        private final String id;
        private final String name;
        private volatile boolean approved;

        private CompanyRecord(String id, String name, boolean approved) {
            this.id = id;
            this.name = name;
            this.approved = approved;
        }

        String id() {
            return id;
        }

        @SuppressWarnings("unused")
        String name() {
            return name;
        }

        boolean approved() {
            return approved;
        }

        void setApproved(boolean approved) {
            this.approved = approved;
        }
    }
}
