package com.kfpcl.controller;

import com.kfpcl.dto.ApiResponse;
import com.kfpcl.dto.AssignUserRoleDto;
import com.kfpcl.dto.UserRoleResponseDto;
import com.kfpcl.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/roles")
@RequiredArgsConstructor
public class AdminRoleController {

    private final AdminUserService adminUserService;

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<List<UserRoleResponseDto>>> assignRoleToUser(
            @Valid @RequestBody AssignUserRoleDto dto) {

        List<UserRoleResponseDto> roles = adminUserService.assignRoleToUser(dto);
        return ResponseEntity.ok(ApiResponse.success(roles, "Role assigned to user successfully"));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserRoleResponseDto>>> getUserRoles(
            @RequestParam String userId) {

        List<UserRoleResponseDto> roles = adminUserService.getUserRoles(userId);
        return ResponseEntity.ok(ApiResponse.success(roles, "User roles retrieved successfully"));
    }
}
