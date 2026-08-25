package com.kfpcl.serviceImpl;

import com.kfpcl.dto.*;
import com.kfpcl.entity.User;
import com.kfpcl.entity.UserRole;
import com.kfpcl.exception.BusinessValidationException;
import com.kfpcl.exception.DuplicateResourceException;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.UserRepository;
import com.kfpcl.repository.UserRoleRepository;
import com.kfpcl.service.AdminUserService;
import com.kfpcl.service.AuditLogService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<AdminUserResponseDto> getUsers(String role, String search, String status, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(role)) {
                try {
                    User.Role uRole = User.Role.valueOf(role.trim().toUpperCase());
                    predicates.add(cb.equal(root.get("role"), uRole));
                } catch (IllegalArgumentException ignored) {}
            }

            if (StringUtils.hasText(status)) {
                try {
                    User.Status uStatus = User.Status.valueOf(status.trim().toUpperCase());
                    predicates.add(cb.equal(root.get("status"), uStatus));
                } catch (IllegalArgumentException ignored) {}
            }

            if (StringUtils.hasText(search)) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("name")), pattern);
                Predicate emailMatch = cb.like(cb.lower(root.get("email")), pattern);
                Predicate phoneMatch = cb.like(cb.lower(root.get("phone")), pattern);
                predicates.add(cb.or(nameMatch, emailMatch, phoneMatch));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<User> userPage = userRepository.findAll(spec, pageable);
        List<AdminUserResponseDto> dtoList = userPage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return PageResponseDto.from(userPage, dtoList);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserResponseDto getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));
        return mapToDto(user);
    }

    @Override
    public AdminUserResponseDto updateUserStatus(String userId, UserStatusUpdateDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

        User.Status newStatus;
        try {
            newStatus = User.Status.valueOf(dto.getStatus().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessValidationException("Invalid user status: " + dto.getStatus() + ". Allowed: ACTIVE, INACTIVE, SUSPENDED");
        }

        String oldStatus = user.getStatus().name();
        user.setStatus(newStatus);
        User saved = userRepository.save(user);

        auditLogService.logAction("admin", "ROLE_ADMIN", "UPDATE_STATUS", "USER", userId, oldStatus, newStatus.name(), null, null);

        return mapToDto(saved);
    }

    @Override
    public AdminUserResponseDto updateUserRole(String userId, UserRoleUpdateDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

        User.Role newRole;
        try {
            newRole = User.Role.valueOf(dto.getRole().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessValidationException("Invalid user role: " + dto.getRole() + ". Allowed: ADMIN, BUYER, SUPPLIER");
        }

        String oldRole = user.getRole().name();
        user.setRole(newRole);
        User saved = userRepository.save(user);

        auditLogService.logAction("admin", "ROLE_ADMIN", "UPDATE_ROLE", "USER", userId, oldRole, newRole.name(), null, null);

        return mapToDto(saved);
    }

    @Override
    public List<UserRoleResponseDto> assignRoleToUser(AssignUserRoleDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", dto.getUserId()));

        String role = dto.getRole().trim().toUpperCase();
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }

        if (userRoleRepository.existsByUserIdAndRole(user.getId(), role)) {
            throw new DuplicateResourceException("UserRole", "role", role);
        }

        UserRole userRole = UserRole.builder()
                .id("urole_" + UUID.randomUUID().toString().substring(0, 8))
                .userId(user.getId())
                .role(role)
                .build();

        userRoleRepository.save(userRole);
        auditLogService.logAction("admin", "ROLE_ADMIN", "ASSIGN_ROLE", "USER_ROLE", user.getId(), null, role, null, null);

        return getUserRoles(user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserRoleResponseDto> getUserRoles(String userId) {
        return userRoleRepository.findByUserId(userId).stream()
                .map(r -> UserRoleResponseDto.builder()
                        .id(r.getId())
                        .userId(r.getUserId())
                        .role(r.getRole())
                        .createdAt(r.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private AdminUserResponseDto mapToDto(User user) {
        List<String> permissions = userRoleRepository.findByUserId(user.getId()).stream()
                .map(UserRole::getRole)
                .collect(Collectors.toList());

        return AdminUserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .permissions(permissions)
                .status(user.getStatus().name())
                .region(user.getRegion())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
