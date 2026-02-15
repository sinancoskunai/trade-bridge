package com.tradebridge.backend.auth;

import java.time.Instant;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.tradebridge.backend.common.UserRole;
import com.tradebridge.backend.persistence.CompanyEntity;
import com.tradebridge.backend.persistence.CompanyRepository;
import com.tradebridge.backend.persistence.UserAccountEntity;
import com.tradebridge.backend.persistence.UserAccountRepository;

import jakarta.annotation.PostConstruct;

@Component
public class AuthBootstrap {

    private final CompanyRepository companyRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthBootstrap(
            CompanyRepository companyRepository,
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder) {
        this.companyRepository = companyRepository;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void ensureAdminUser() {
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
}
