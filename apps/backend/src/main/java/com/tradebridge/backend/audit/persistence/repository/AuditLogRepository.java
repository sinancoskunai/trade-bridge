package com.tradebridge.backend.audit.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tradebridge.backend.audit.persistence.entity.AuditLogEntity;

public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {
}
