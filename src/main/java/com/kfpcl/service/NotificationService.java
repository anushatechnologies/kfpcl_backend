package com.kfpcl.service;

import com.kfpcl.dto.PushTokenRegisterDto;
import com.kfpcl.entity.PushToken;

public interface NotificationService {
    PushToken registerToken(String userId, PushTokenRegisterDto dto);
}
