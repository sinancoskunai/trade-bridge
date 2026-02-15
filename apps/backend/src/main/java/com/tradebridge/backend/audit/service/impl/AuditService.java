package com.tradebridge.backend.audit;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.tradebridge.backend.audit.persistence.entity.AuditLogEntity;
import com.tradebridge.backend.audit.persistence.repository.AuditLogRepository;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String actorUserId, String companyId, String action, String details) {
        AuditLogEntity log = new AuditLogEntity();
        log.setActorUserId(actorUserId);
        log.setCompanyId(companyId);
        log.setAction(action);
        log.setDetails(details);
        log.setCreatedAt(Instant.now());
        auditLogRepository.save(log);
    }
}
