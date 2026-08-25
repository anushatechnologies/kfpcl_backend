package com.kfpcl.service;

import com.kfpcl.dto.*;

import java.util.List;

public interface AdminUserService {

    PageResponseDto<AdminUserResponseDto> getUsers(String role, String search, String status, int page, int size, String sortBy, String sortDir);

    AdminUserResponseDto getUserById(String userId);

    AdminUserResponseDto updateUserStatus(String userId, UserStatusUpdateDto dto);

    AdminUserResponseDto updateUserRole(String userId, UserRoleUpdateDto dto);

    List<UserRoleResponseDto> assignRoleToUser(AssignUserRoleDto dto);

    List<UserRoleResponseDto> getUserRoles(String userId);
}
