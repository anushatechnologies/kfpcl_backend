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
            "SUPER_ADMIN",
            "ADMIN_ALL",
            // User Management
            "ADMIN_USERS_READ",
            "ADMIN_USERS_WRITE",
            "ADMIN_USERS_CREATE",
            "ADMIN_USERS_UPDATE",
            "ADMIN_USERS_DELETE",
            // Products Management
            "ADMIN_PRODUCTS_READ",
            "ADMIN_PRODUCTS_WRITE",
            "ADMIN_PRODUCTS_CREATE",
            "ADMIN_PRODUCTS_UPDATE",
            "ADMIN_PRODUCTS_EDIT",
            "ADMIN_PRODUCTS_DELETE",
            "ADMIN_PRODUCTS_APPROVE",
            "ADMIN_PRODUCTS_REJECT",
            // Catalog Management (Categories, Subcategories, Brands)
            "ADMIN_CATALOG_READ",
            "ADMIN_CATALOG_WRITE",
            "ADMIN_CATEGORIES_READ",
            "ADMIN_CATEGORIES_WRITE",
            "ADMIN_CATEGORIES_CREATE",
            "ADMIN_CATEGORIES_UPDATE",
            "ADMIN_CATEGORIES_DELETE",
            "ADMIN_SUBCATEGORIES_READ",
            "ADMIN_SUBCATEGORIES_WRITE",
            "ADMIN_SUBCATEGORIES_CREATE",
            "ADMIN_SUBCATEGORIES_UPDATE",
            "ADMIN_SUBCATEGORIES_DELETE",
            "ADMIN_BRANDS_READ",
            "ADMIN_BRANDS_WRITE",
            "ADMIN_BRANDS_CREATE",
            "ADMIN_BRANDS_UPDATE",
            "ADMIN_BRANDS_DELETE",
            // Inventory Management
            "ADMIN_INVENTORY_READ",
            "ADMIN_INVENTORY_WRITE",
            "ADMIN_INVENTORY_UPDATE",
            // Orders & Logistics
            "ADMIN_ORDERS_READ",
            "ADMIN_ORDERS_WRITE",
            "ADMIN_ORDERS_UPDATE",
            "ADMIN_ORDERS_DELETE",
            // Sellers & Buyers
            "ADMIN_SELLERS_READ",
            "ADMIN_SELLERS_WRITE",
            "ADMIN_SELLERS_APPROVE",
            "ADMIN_SELLERS_REJECT",
            "ADMIN_BUYERS_READ",
            "ADMIN_BUYERS_WRITE",
            // RFQs & Quotations
            "ADMIN_RFQS_READ",
            "ADMIN_RFQS_WRITE",
            "ADMIN_QUOTATIONS_READ",
            "ADMIN_QUOTATIONS_WRITE",
            // Reviews & Moderation
            "ADMIN_REVIEWS_READ",
            "ADMIN_REVIEWS_WRITE",
            "ADMIN_REVIEWS_APPROVE",
            "ADMIN_REVIEWS_REJECT",
            // Support & Tickets
            "ADMIN_SUPPORT_READ",
            "ADMIN_SUPPORT_WRITE",
            "ADMIN_SUPPORT_REPLY",
            // Notifications & Broadcasts
            "ADMIN_NOTIFICATIONS_READ",
            "ADMIN_NOTIFICATIONS_WRITE",
            "ADMIN_NOTIFICATIONS_SEND",
            // Analytics & Audit Logs
            "ADMIN_ANALYTICS_READ",
            "ADMIN_AUDIT_READ",
            // Platform Settings
            "ADMIN_SETTINGS_READ",
            "ADMIN_SETTINGS_WRITE"
    );

    @PostConstruct
    public void initDefaultAdmin() {
        User admin = userRepository.findByEmail("admin@kfpcl.com").orElse(null);
        if (admin == null) {
            admin = User.builder()
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
        }

        // Ensure all super admin permissions are present
        List<String> existingRoles = userRoleRepository.findByUserId(admin.getId()).stream()
                .map(UserRole::getRole)
                .collect(Collectors.toList());

        for (String perm : DEFAULT_ADMIN_PERMISSIONS) {
            if (!existingRoles.contains(perm)) {
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

        if (permissions.isEmpty() || user.getEmail().equalsIgnoreCase("admin@kfpcl.com")) {
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

        if (permissions.isEmpty() || user.getEmail().equalsIgnoreCase("admin@kfpcl.com")) {
            permissions = DEFAULT_ADMIN_PERMISSIONS;
        }

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
