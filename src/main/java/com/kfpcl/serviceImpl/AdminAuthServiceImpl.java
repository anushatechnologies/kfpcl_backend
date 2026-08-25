package com.kfpcl.serviceImpl;

import com.kfpcl.dto.AdminLoginDto;
import com.kfpcl.dto.AdminLoginResponseDto;
import com.kfpcl.dto.AdminProfileResponseDto;
import com.kfpcl.entity.User;
import com.kfpcl.entity.UserRole;
import com.kfpcl.exception.UnauthorizedException;
import com.kfpcl.repository.UserRepository;
import com.kfpcl.repository.UserRoleRepository;
import com.kfpcl.service.AdminAuthService;
import com.kfpcl.service.AuditLogService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminAuthServiceImpl implements AdminAuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final AuditLogService auditLogService;

    private static final List<String> DEFAULT_ADMIN_PERMISSIONS = Arrays.asList(
            "ADMIN",
            "ADMIN_USERS_READ",
            "ADMIN_USERS_UPDATE",
            "ADMIN_PRODUCTS_READ",
            "ADMIN_PRODUCTS_APPROVE",
            "ADMIN_ORDERS_READ",
            "ADMIN_ORDERS_UPDATE",
            "ADMIN_ANALYTICS_READ",
            "ADMIN_AUDIT_READ"
    );

    @PostConstruct
    public void initDefaultAdmin() {
        if (!userRepository.existsByEmail("admin@kfpcl.com")) {
            User admin = User.builder()
                    .id("user_admin_default")
                    .name("KFPCL Super Admin")
                    .email("admin@kfpcl.com")
                    .phone("+91-9876543210")
                    .password("admin123")
                    .role(User.Role.ADMIN)
                    .status(User.Status.ACTIVE)
                    .region("Headquarters")
                    .build();
            userRepository.save(admin);

            for (String perm : DEFAULT_ADMIN_PERMISSIONS) {
                userRoleRepository.save(UserRole.builder()
                        .id("role_" + UUID.randomUUID().toString().substring(0, 8))
                        .userId(admin.getId())
                        .role(perm)
                        .build());
            }
        }
    }

    @Override
    public AdminLoginResponseDto login(AdminLoginDto dto) {
        String email = dto.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!user.getPassword().equals(dto.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (user.getRole() != User.Role.ADMIN) {
            throw new UnauthorizedException("Access denied: Not an administrator account");
        }

        if (user.getStatus() != User.Status.ACTIVE) {
            throw new UnauthorizedException("Admin account is " + user.getStatus());
        }

        List<String> permissions = userRoleRepository.findByUserId(user.getId()).stream()
                .map(UserRole::getRole)
                .collect(Collectors.toList());

        if (permissions.isEmpty()) {
            permissions = DEFAULT_ADMIN_PERMISSIONS;
        }

        auditLogService.logAction(user.getId(), "ROLE_ADMIN", "LOGIN", "AUTH", user.getId(), null, "SUCCESS", null, null);

        return AdminLoginResponseDto.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .permissions(permissions)
                .build();
    }

    @Override
    public void logout() {
        auditLogService.logAction("admin", "ROLE_ADMIN", "LOGOUT", "AUTH", "admin", null, "LOGGED_OUT", null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminProfileResponseDto getCurrentAdmin() {
        User user = userRepository.findByEmail("admin@kfpcl.com")
                .orElseThrow(() -> new UnauthorizedException("Admin user not found"));

        List<String> permissions = userRoleRepository.findByUserId(user.getId()).stream()
                .map(UserRole::getRole)
                .collect(Collectors.toList());

        return AdminProfileResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .permissions(permissions)
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
