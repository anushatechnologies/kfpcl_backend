package com.kfpcl.controller;

import com.kfpcl.dto.ApiResponse;
import com.kfpcl.dto.NotificationCreateDto;
import com.kfpcl.dto.NotificationResponseDto;
import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.service.PlatformNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final PlatformNotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDto<NotificationResponseDto>>> listNotifications(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String audience,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        PageResponseDto<NotificationResponseDto> notifications = notificationService.getNotifications(status, audience, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(notifications, "Notifications retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NotificationResponseDto>> createNotification(
            @Valid @RequestBody NotificationCreateDto dto) {

        NotificationResponseDto created = notificationService.createNotification(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Notification created successfully"));
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<NotificationResponseDto>> getNotification(
            @PathVariable String notificationId) {

        NotificationResponseDto notification = notificationService.getNotificationById(notificationId);
        return ResponseEntity.ok(ApiResponse.success(notification, "Notification details retrieved successfully"));
    }

    @PostMapping("/{notificationId}/send")
    public ResponseEntity<ApiResponse<NotificationResponseDto>> sendNotification(
            @PathVariable String notificationId) {

        NotificationResponseDto sent = notificationService.sendNotification(notificationId);
        return ResponseEntity.ok(ApiResponse.success(sent, "Notification dispatched successfully"));
    }

    @PostMapping("/{notificationId}/cancel")
    public ResponseEntity<ApiResponse<NotificationResponseDto>> cancelNotification(
            @PathVariable String notificationId) {

        NotificationResponseDto cancelled = notificationService.cancelNotification(notificationId);
        return ResponseEntity.ok(ApiResponse.success(cancelled, "Notification cancelled successfully"));
    }
}
