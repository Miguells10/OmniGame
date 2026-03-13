package com.omnigame.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service responsible for recording security events (Audit Trail).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityAuditService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Logs an access or security event.
     */
    public void logEvent(UUID userId, String authUid, String action, String resource, String status, String ipAddress, String details) {
        log.info("AUDIT EVENT: user={}, action={}, status={}, resource={}", authUid, action, status, resource);
        
        String sql = "INSERT INTO audit_logs (user_id, auth_uid, action, resource, status, ip_address, details) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            jdbcTemplate.update(sql, userId, authUid, action, resource, status, ipAddress, details);
        } catch (Exception e) {
            log.error("Failed to write audit log to database", e);
        }
    }
}
