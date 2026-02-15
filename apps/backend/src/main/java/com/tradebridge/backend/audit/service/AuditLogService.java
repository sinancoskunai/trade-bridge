package com.tradebridge.backend.audit.service;

public interface AuditLogService {

    void log(String actorUserId, String companyId, String action, String details);
}
