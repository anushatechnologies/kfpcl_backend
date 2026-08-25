package com.kfpcl.service;

import com.kfpcl.dto.AdminUserResponseDto;
import com.kfpcl.dto.BuyerActivityDto;
import com.kfpcl.dto.BuyerDetailResponseDto;
import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.dto.UserStatusUpdateDto;

import java.util.List;

public interface AdminBuyerService {

    PageResponseDto<AdminUserResponseDto> getBuyers(String search, String status, int page, int size, String sortBy, String sortDir);

    BuyerDetailResponseDto getBuyerById(String buyerId);

    List<BuyerActivityDto> getBuyerActivity(String buyerId);

    BuyerDetailResponseDto updateBuyerStatus(String buyerId, UserStatusUpdateDto dto);
}
