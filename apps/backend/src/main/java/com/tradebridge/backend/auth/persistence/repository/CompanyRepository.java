package com.tradebridge.backend.auth.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tradebridge.backend.auth.persistence.entity.CompanyEntity;

public interface CompanyRepository extends JpaRepository<CompanyEntity, String> {
}
