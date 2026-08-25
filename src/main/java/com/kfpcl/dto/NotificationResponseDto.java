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
public class NotificationResponseDto {

    private String id;
    private String title;
    private String message;
    private String audience;
    private String channels;
    private LocalDateTime scheduledAt;
    private String status;
    private LocalDateTime dispatchedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
