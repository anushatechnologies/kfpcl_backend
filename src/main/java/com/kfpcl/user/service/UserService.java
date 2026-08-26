package com.kfpcl.user.service;

import com.kfpcl.user.dto.BuyerProfileResponseDto;
import com.kfpcl.user.dto.BuyerProfileUpdateDto;

public interface UserService {

    BuyerProfileResponseDto getBuyerProfile(String userId);

    BuyerProfileResponseDto updateBuyerProfile(String userId, BuyerProfileUpdateDto updateDto);

    void deleteAccount(String userId, String currentSessionId);
}
