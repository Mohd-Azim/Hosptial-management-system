package com.hospital.hms.service;

import com.hospital.hms.domain.AuditLog;
import com.hospital.hms.repo.AuditLogRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordHttpAction(
            Long userId,
            String username,
            String method,
            String requestUri,
            int responseStatus,
            String clientIp,
            String detail) {
        AuditLog row = new AuditLog();
        row.setCreatedAt(Instant.now());
        row.setUserId(userId);
        row.setUsername(trunc(username, 128));
        row.setHttpMethod(trunc(method, 16));
        row.setRequestUri(trunc(requestUri, 512));
        row.setResponseStatus(responseStatus);
        row.setClientIp(trunc(clientIp, 64));
        row.setDetail(trunc(detail, 512));
        auditLogRepository.save(row);
    }

    private static String trunc(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}
