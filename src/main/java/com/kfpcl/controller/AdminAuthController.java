package com.kfpcl.controller;

import com.kfpcl.dto.AdminLoginDto;
import com.kfpcl.dto.AdminLoginResponseDto;
import com.kfpcl.dto.AdminProfileResponseDto;
import com.kfpcl.dto.ApiResponse;
import com.kfpcl.service.AdminAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AdminLoginResponseDto>> login(@Valid @RequestBody AdminLoginDto dto) {
        AdminLoginResponseDto response = adminAuthService.login(dto);
        return ResponseEntity.ok(ApiResponse.success(response, "Admin authenticated successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        adminAuthService.logout();
        return ResponseEntity.ok(ApiResponse.success(null, "Admin logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AdminProfileResponseDto>> getMe() {
        AdminProfileResponseDto profile = adminAuthService.getCurrentAdmin();
        return ResponseEntity.ok(ApiResponse.success(profile, "Admin profile retrieved successfully"));
    }
}
