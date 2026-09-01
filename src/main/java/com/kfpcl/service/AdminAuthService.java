package com.kfpcl.service;

import com.kfpcl.dto.AdminLoginDto;
import com.kfpcl.dto.AdminLoginResponseDto;
import com.kfpcl.dto.AdminProfileResponseDto;

import com.kfpcl.dto.AdminRefreshDto;

public interface AdminAuthService {

    AdminLoginResponseDto login(AdminLoginDto dto);

    void logout();

    AdminProfileResponseDto getCurrentAdmin();

    AdminLoginResponseDto refresh(AdminRefreshDto dto);
}
