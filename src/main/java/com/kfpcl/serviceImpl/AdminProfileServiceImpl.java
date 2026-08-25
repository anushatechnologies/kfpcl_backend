package com.kfpcl.serviceImpl;

import com.kfpcl.dto.AdminProfileResponseDto;
import com.kfpcl.dto.AdminProfileUpdateDto;
import com.kfpcl.dto.PlatformSettingsUpdateDto;
import com.kfpcl.entity.PlatformSetting;
import com.kfpcl.entity.User;
import com.kfpcl.entity.UserRole;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.PlatformSettingRepository;
import com.kfpcl.repository.UserRepository;
import com.kfpcl.repository.UserRoleRepository;
import com.kfpcl.service.AdminProfileService;
import com.kfpcl.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminProfileServiceImpl implements AdminProfileService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PlatformSettingRepository platformSettingRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional(readOnly = true)
    public AdminProfileResponseDto getProfile(String adminId) {
        String id = StringUtils.hasText(adminId) ? adminId : "user_admin_default";
        User user = userRepository.findById(id)
                .orElseGet(() -> userRepository.findByEmail("admin@kfpcl.com")
                        .orElseThrow(() -> new ResourceNotFoundException("AdminProfile", "adminId", id)));

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

    @Override
    public AdminProfileResponseDto updateProfile(String adminId, AdminProfileUpdateDto dto) {
        String id = StringUtils.hasText(adminId) ? adminId : "user_admin_default";
        User user = userRepository.findById(id)
                .orElseGet(() -> userRepository.findByEmail("admin@kfpcl.com")
                        .orElseThrow(() -> new ResourceNotFoundException("AdminProfile", "adminId", id)));

        if (StringUtils.hasText(dto.getName())) {
            user.setName(dto.getName().trim());
        }
        if (StringUtils.hasText(dto.getPhone())) {
            user.setPhone(dto.getPhone().trim());
        }
        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(dto.getPassword().trim());
        }

        User saved = userRepository.save(user);
        auditLogService.logAction(user.getId(), "ROLE_ADMIN", "UPDATE_PROFILE", "ADMIN_PROFILE", user.getId(), null, "UPDATED", null, null);

        return getProfile(saved.getId());
    }

    @Override
    public Map<String, String> updateSettings(PlatformSettingsUpdateDto dto) {
        if (dto != null && dto.getSettings() != null) {
            for (Map.Entry<String, String> entry : dto.getSettings().entrySet()) {
                String key = entry.getKey();
                String val = entry.getValue();

                PlatformSetting setting = platformSettingRepository.findBySettingKey(key)
                        .orElse(PlatformSetting.builder()
                                .id("setting_" + UUID.randomUUID().toString().substring(0, 8))
                                .settingKey(key)
                                .build());

                setting.setSettingValue(val);
                platformSettingRepository.save(setting);
            }
            auditLogService.logAction("admin", "ROLE_ADMIN", "UPDATE_SETTINGS", "PLATFORM_SETTINGS", "global", null, "UPDATED", null, null);
        }
        return getSettings();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, String> getSettings() {
        Map<String, String> map = new LinkedHashMap<>();
        for (PlatformSetting s : platformSettingRepository.findAll()) {
            map.put(s.getSettingKey(), s.getSettingValue());
        }
        if (map.isEmpty()) {
            map.put("siteName", "KFPCL B2B Marketplace");
            map.put("currency", "INR");
            map.put("commissionRate", "2.5%");
            map.put("escrowHoldingDays", "7");
            map.put("maintenanceMode", "false");
        }
        return map;
    }
}
