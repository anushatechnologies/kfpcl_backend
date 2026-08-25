package com.kfpcl.service;

import com.kfpcl.dto.AdminProfileResponseDto;
import com.kfpcl.dto.AdminProfileUpdateDto;
import com.kfpcl.dto.PlatformSettingsUpdateDto;

import java.util.Map;

public interface AdminProfileService {

    AdminProfileResponseDto getProfile(String adminId);

    AdminProfileResponseDto updateProfile(String adminId, AdminProfileUpdateDto dto);

    Map<String, String> updateSettings(PlatformSettingsUpdateDto dto);

    Map<String, String> getSettings();
}
