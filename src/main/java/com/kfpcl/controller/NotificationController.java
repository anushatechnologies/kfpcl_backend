package com.kfpcl.controller;

import com.kfpcl.dto.PushTokenRegisterDto;
import com.kfpcl.entity.PushToken;
import com.kfpcl.service.NotificationService;
import jakarta.validation.Valid;
import com.kfpcl.dto.ApiResponse;
import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.dto.UserNotificationDto;
import com.kfpcl.dto.NotificationPreferenceDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/register-token")
    public ResponseEntity<PushToken> registerToken(
            @Valid @RequestBody PushTokenRegisterDto request,
            @RequestHeader(value = "UserId", defaultValue = "user_1") String userId) {
        PushToken saved = notificationService.registerToken(userId, request);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDto<UserNotificationDto>>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        String userId = (String) request.getAttribute("authenticatedUser");
        PageResponseDto<UserNotificationDto> response = notificationService.getUserNotifications(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response, "Notifications retrieved successfully"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<UserNotificationDto>> markAsRead(
            @PathVariable String id,
            HttpServletRequest request) {
        String userId = (String) request.getAttribute("authenticatedUser");
        UserNotificationDto response = notificationService.markNotificationAsRead(userId, id);
        return ResponseEntity.ok(ApiResponse.success(response, "Notification marked as read"));
    }

    @GetMapping("/preferences")
    public ResponseEntity<ApiResponse<NotificationPreferenceDto>> getPreferences(HttpServletRequest request) {
        String userId = (String) request.getAttribute("authenticatedUser");
        NotificationPreferenceDto response = notificationService.getPreferences(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Preferences retrieved successfully"));
    }

    @PutMapping("/preferences")
    public ResponseEntity<ApiResponse<NotificationPreferenceDto>> updatePreferences(
            @RequestBody NotificationPreferenceDto dto,
            HttpServletRequest request) {
        String userId = (String) request.getAttribute("authenticatedUser");
        NotificationPreferenceDto response = notificationService.updatePreferences(userId, dto);
        return ResponseEntity.ok(ApiResponse.success(response, "Preferences updated successfully"));
    }
}
