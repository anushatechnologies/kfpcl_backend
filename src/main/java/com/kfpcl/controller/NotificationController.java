package com.kfpcl.controller;

import com.kfpcl.dto.PushTokenRegisterDto;
import com.kfpcl.entity.PushToken;
import com.kfpcl.service.NotificationService;
import jakarta.validation.Valid;
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
}
