package com.internship.tool.service;

import com.internship.tool.entity.AuditLog;
import com.internship.tool.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditRepo;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String entityType, Long entityId, String action,
                    Long performedByUserId, Map<String, Object> oldValue,
                    Map<String, Object> newValue) {
        try {
            AuditLog entry = AuditLog.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .performedById(performedByUserId)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .build();
            auditRepo.save(entry);
        } catch (Exception e) {
            log.error("Audit log failed for {} #{} action {}: {}", entityType, entityId, action, e.getMessage());
        }
    }
}
