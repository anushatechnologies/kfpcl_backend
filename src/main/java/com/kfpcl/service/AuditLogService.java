package com.kfpcl.service;

import com.kfpcl.dto.AuditLogResponseDto;
import com.kfpcl.dto.PageResponseDto;

public interface AuditLogService {

    void logAction(String actorId, String actorRole, String action, String entityType, String entityId, String previousValue, String newValue, String ipAddress, String deviceInfo);

    PageResponseDto<AuditLogResponseDto> getAuditLogs(String entityType, String action, String from, String to, int page, int size, String sortBy, String sortDir);
}
