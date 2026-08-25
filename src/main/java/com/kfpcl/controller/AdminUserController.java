package com.kfpcl.controller;

import com.kfpcl.dto.*;
import com.kfpcl.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDto<AdminUserResponseDto>>> listUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        PageResponseDto<AdminUserResponseDto> users = adminUserService.getUsers(role, search, status, page, limit, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<AdminUserResponseDto>> getUser(@PathVariable String userId) {
        AdminUserResponseDto user = adminUserService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(user, "User details retrieved successfully"));
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<AdminUserResponseDto>> updateUserStatus(
            @PathVariable String userId,
            @Valid @RequestBody UserStatusUpdateDto dto) {

        AdminUserResponseDto updated = adminUserService.updateUserStatus(userId, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "User status updated successfully"));
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<ApiResponse<AdminUserResponseDto>> updateUserRole(
            @PathVariable String userId,
            @Valid @RequestBody UserRoleUpdateDto dto) {

        AdminUserResponseDto updated = adminUserService.updateUserRole(userId, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "User role updated successfully"));
    }
}
