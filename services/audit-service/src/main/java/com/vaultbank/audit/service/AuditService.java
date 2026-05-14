package com.vaultbank.audit.service;

import com.vaultbank.audit.entity.AuditLog;
import com.vaultbank.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void record(String eventType, Long userId, String entityType,
                       String entityId, String action, String details) {
        AuditLog entry = AuditLog.builder()
                .eventType(eventType)
                .userId(userId)
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .details(details)
                .build();

        auditLogRepository.save(entry);
        log.info("Auditoria: type={} user={} entity={}/{} action={}",
                eventType, userId, entityType, entityId, action);
    }

    @Transactional
    public void recordTransaction(Long transactionId, Long userId, String type, String details) {
        record("TRANSACTION", userId, "TRANSACTION", transactionId.toString(), type, details);
    }

    @Transactional
    public void recordFraud(Long transactionId, Long userId, String details) {
        record("FRAUD_DETECTED", userId, "TRANSACTION", transactionId.toString(), "FRAUD_ALERT", details);
        log.warn("FRAUDE DETECTADA - tx={} user={}", transactionId, userId);
    }
}
