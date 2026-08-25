package com.kfpcl.service;

import com.kfpcl.dto.AdminLoginDto;
import com.kfpcl.dto.AdminLoginResponseDto;
import com.kfpcl.dto.AdminProfileResponseDto;

public interface AdminAuthService {

    AdminLoginResponseDto login(AdminLoginDto dto);

    void logout();

    AdminProfileResponseDto getCurrentAdmin();
}
