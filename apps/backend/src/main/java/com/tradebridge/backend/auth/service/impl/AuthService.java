package com.tradebridge.backend.auth.service.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradebridge.backend.audit.service.AuditLogService;
import com.tradebridge.backend.common.ApiException;
import com.tradebridge.backend.common.UserRole;
import com.tradebridge.backend.jwt.JwtService;
import com.tradebridge.backend.auth.model.AuthenticatedUser;
import com.tradebridge.backend.auth.model.CompanyApprovalResponse;
import com.tradebridge.backend.auth.model.LoginRequest;
import com.tradebridge.backend.auth.model.RefreshRequest;
import com.tradebridge.backend.auth.model.RegisterCompanyRequest;
import com.tradebridge.backend.auth.model.TokenResponse;
import com.tradebridge.backend.auth.model.UserProfileResponse;
import com.tradebridge.backend.auth.persistence.entity.CompanyEntity;
import com.tradebridge.backend.auth.persistence.repository.CompanyRepository;
import com.tradebridge.backend.auth.persistence.entity.RefreshTokenEntity;
import com.tradebridge.backend.auth.persistence.repository.RefreshTokenRepository;
import com.tradebridge.backend.auth.persistence.entity.UserAccountEntity;
import com.tradebridge.backend.auth.persistence.repository.UserAccountRepository;
import com.tradebridge.backend.auth.service.AuthApplicationService;

@Service
public class AuthService implements AuthApplicationService {

    private final CompanyRepository companyRepository;
    private final UserAccountRepository userAccountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditLogService auditService;
    private final Duration accessTokenTtl;
    private final Duration refreshTokenTtl;

    public AuthService(
            CompanyRepository companyRepository,
            UserAccountRepository userAccountRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuditLogService auditService,
            @Value("${app.security.access-token-minutes}") long accessTokenMinutes,
            @Value("${app.security.refresh-token-days}") long refreshTokenDays) {
        this.companyRepository = companyRepository;
        this.userAccountRepository = userAccountRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditService = auditService;
        this.accessTokenTtl = Duration.ofMinutes(accessTokenMinutes);
        this.refreshTokenTtl = Duration.ofDays(refreshTokenDays);
    }

    @Override
    @Transactional
    public CompanyApprovalResponse registerCompany(RegisterCompanyRequest request) {
        if (request.role() == UserRole.ADMIN) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ADMIN role cannot self-register");
        }
        userAccountRepository.findByEmailIgnoreCase(request.email()).ifPresent(existing -> {
            throw new ApiException(HttpStatus.CONFLICT, "Email already in use");
        });

        CompanyEntity company = new CompanyEntity();
        company.setId(UUID.randomUUID().toString());
        company.setName(request.companyName());
        company.setApproved(false);
        company.setCreatedAt(Instant.now());
        company.setUpdatedAt(Instant.now());
        companyRepository.save(company);

        UserAccountEntity user = new UserAccountEntity();
        user.setId(UUID.randomUUID().toString());
        user.setCompany(company);
        user.setEmail(request.email().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setActive(true);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        userAccountRepository.save(user);

        auditService.log(user.getId(), company.getId(), "COMPANY_REGISTERED", request.companyName());
        return new CompanyApprovalResponse(company.getId(), false);
    }

    @Override
    @Transactional
    public CompanyApprovalResponse approveCompany(String companyId, AuthenticatedUser adminUser) {
        if (adminUser.role() != UserRole.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only ADMIN can approve companies");
        }

        CompanyEntity company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Company not found"));
        company.setApproved(true);
        company.setUpdatedAt(Instant.now());
        companyRepository.save(company);

        auditService.log(adminUser.userId(), companyId, "COMPANY_APPROVED", "approved=true");
        return new CompanyApprovalResponse(company.getId(), true);
    }

    @Override
    @Transactional
    public TokenResponse login(LoginRequest request) {
        UserAccountEntity user = userAccountRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        if (!user.isActive()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "User is inactive");
        }
        if (user.getRole() != UserRole.ADMIN && !user.getCompany().isApproved()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Company is not approved yet");
        }

        AuthenticatedUser authUser = new AuthenticatedUser(
                user.getId(),
                user.getCompany().getId(),
                user.getEmail(),
                user.getRole(),
                user.getCompany().isApproved());

        String accessToken = jwtService.issueAccessToken(authUser, accessTokenTtl);
        String refreshToken = issueRefreshToken(user);
        auditService.log(user.getId(), user.getCompany().getId(), "LOGIN_SUCCESS", "email=" + user.getEmail());

        return new TokenResponse(accessToken, refreshToken, user.getId(), user.getCompany().getId(), user.getRole());
    }

    @Override
    @Transactional
    public TokenResponse refresh(RefreshRequest request) {
        RefreshTokenEntity storedToken = refreshTokenRepository.findById(request.refreshToken())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.deleteById(storedToken.getToken());
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }

        UserAccountEntity user = storedToken.getUser();
        refreshTokenRepository.deleteById(storedToken.getToken());

        AuthenticatedUser authUser = new AuthenticatedUser(
                user.getId(),
                user.getCompany().getId(),
                user.getEmail(),
                user.getRole(),
                user.getCompany().isApproved());

        String accessToken = jwtService.issueAccessToken(authUser, accessTokenTtl);
        String refreshToken = issueRefreshToken(user);

        return new TokenResponse(accessToken, refreshToken, user.getId(), user.getCompany().getId(), user.getRole());
    }

    @Override
    public UserProfileResponse profile(AuthenticatedUser user) {
        return new UserProfileResponse(user.userId(), user.companyId(), user.email(), user.role(), user.companyApproved());
    }

    @Override
    @Transactional
    public void ensurePlatformAdminUser() {
        String adminEmail = "admin@tradebridge.local";
        if (userAccountRepository.findByEmailIgnoreCase(adminEmail).isPresent()) {
            return;
        }

        CompanyEntity platform = new CompanyEntity();
        platform.setId(UUID.randomUUID().toString());
        platform.setName("Trade Bridge Platform");
        platform.setApproved(true);
        platform.setCreatedAt(Instant.now());
        platform.setUpdatedAt(Instant.now());
        companyRepository.save(platform);

        UserAccountEntity admin = new UserAccountEntity();
        admin.setId(UUID.randomUUID().toString());
        admin.setCompany(platform);
        admin.setEmail(adminEmail);
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        admin.setRole(UserRole.ADMIN);
        admin.setActive(true);
        admin.setCreatedAt(Instant.now());
        admin.setUpdatedAt(Instant.now());
        userAccountRepository.save(admin);
    }

    private String issueRefreshToken(UserAccountEntity user) {
        String refreshToken = "rt_" + UUID.randomUUID();

        RefreshTokenEntity tokenEntity = new RefreshTokenEntity();
        tokenEntity.setToken(refreshToken);
        tokenEntity.setUser(user);
        tokenEntity.setCreatedAt(Instant.now());
        tokenEntity.setExpiresAt(Instant.now().plus(refreshTokenTtl));
        refreshTokenRepository.save(tokenEntity);

        return refreshToken;
    }
}
