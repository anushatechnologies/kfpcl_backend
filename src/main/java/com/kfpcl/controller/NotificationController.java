package com.kfpcl.controller;

import com.kfpcl.dto.PushTokenRegisterDto;
import com.kfpcl.entity.PushToken;
import com.kfpcl.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
            @Valid @RequestBody PushTokenRegisterDto dto,
            Authentication authentication) {
        String userId = authentication != null ? authentication.getName() : "user_1";
        PushToken saved = notificationService.registerToken(userId, dto);
        return ResponseEntity.ok(saved);
    }
}
