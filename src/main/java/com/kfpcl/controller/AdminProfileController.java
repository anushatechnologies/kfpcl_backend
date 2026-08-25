package com.kfpcl.controller;

import com.kfpcl.dto.AdminProfileResponseDto;
import com.kfpcl.dto.AdminProfileUpdateDto;
import com.kfpcl.dto.ApiResponse;
import com.kfpcl.dto.PlatformSettingsUpdateDto;
import com.kfpcl.service.AdminProfileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminProfileController {

    private final AdminProfileService adminProfileService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<AdminProfileResponseDto>> getProfile(HttpServletRequest request) {
        String adminId = (String) request.getAttribute("authenticatedUser");
        AdminProfileResponseDto profile = adminProfileService.getProfile(adminId);
        return ResponseEntity.ok(ApiResponse.success(profile, "Admin profile retrieved successfully"));
    }

    @PatchMapping("/profile")
    public ResponseEntity<ApiResponse<AdminProfileResponseDto>> updateProfile(
            @RequestBody AdminProfileUpdateDto dto,
            HttpServletRequest request) {

        String adminId = (String) request.getAttribute("authenticatedUser");
        AdminProfileResponseDto updated = adminProfileService.updateProfile(adminId, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Admin profile updated successfully"));
    }

    @PatchMapping("/settings")
    public ResponseEntity<ApiResponse<Map<String, String>>> updateSettings(
            @RequestBody PlatformSettingsUpdateDto dto) {

        Map<String, String> settings = adminProfileService.updateSettings(dto);
        return ResponseEntity.ok(ApiResponse.success(settings, "Platform settings updated successfully"));
    }
}
