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
public class UserNotificationDto {
    private String id;
    private String type;
    private String title;
    private String body;
    private boolean read;
    private String targetPath;
    private LocalDateTime createdAt;
}
