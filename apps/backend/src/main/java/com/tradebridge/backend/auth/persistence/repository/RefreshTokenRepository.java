package com.tradebridge.backend.auth.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tradebridge.backend.auth.persistence.entity.RefreshTokenEntity;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, String> {
}
