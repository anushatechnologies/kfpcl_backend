package com.kfpcl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponseDto {

    private String id;
    private String actorId;
    private String actorRole;
    private String action;
    private String entityType;
    private String entityId;
    private String previousValue;
    private String newValue;
    private String ipAddress;
    private String deviceInfo;
    private LocalDateTime timestamp;
}
