package com.tradebridge.backend.jwt;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tradebridge.backend.auth.AuthenticatedUser;
import com.tradebridge.backend.common.UserRole;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

@Service
public class JwtService {

    private final SecretKey secretKey;

    public JwtService(@Value("${app.security.jwt-secret}") String rawSecret) {
        byte[] bytes;
        try {
            bytes = Decoders.BASE64.decode(rawSecret);
        } catch (Exception ignored) {
            bytes = rawSecret.getBytes(StandardCharsets.UTF_8);
        }
        if (bytes.length < 32) {
            byte[] expanded = new byte[32];
            for (int i = 0; i < expanded.length; i++) {
                expanded[i] = bytes[i % bytes.length];
            }
            bytes = expanded;
        }
        this.secretKey = Keys.hmacShaKeyFor(bytes);
    }

    public String issueAccessToken(AuthenticatedUser user, Duration ttl) {
        Instant now = Instant.now();
        Instant exp = now.plus(ttl);
        return Jwts.builder()
                .subject(user.userId())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("companyId", user.companyId())
                .claim("email", user.email())
                .claim("role", user.role().name())
                .claim("companyApproved", user.companyApproved())
                .signWith(secretKey)
                .compact();
    }

    public AuthenticatedUser parseAccessToken(String token) {
        Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
        String userId = claims.getSubject();
        String companyId = claims.get("companyId", String.class);
        String email = claims.get("email", String.class);
        String role = claims.get("role", String.class);
        Boolean approved = claims.get("companyApproved", Boolean.class);

        return new AuthenticatedUser(userId, companyId, email, UserRole.valueOf(role), Boolean.TRUE.equals(approved));
    }
}
