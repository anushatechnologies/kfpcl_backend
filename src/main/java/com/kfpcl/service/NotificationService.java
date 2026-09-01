package com.kfpcl.service;

import com.kfpcl.dto.PushTokenRegisterDto;
import com.kfpcl.entity.PushToken;
import com.kfpcl.dto.UserNotificationDto;
import com.kfpcl.dto.NotificationPreferenceDto;
import com.kfpcl.dto.PageResponseDto;

public interface NotificationService {
    PushToken registerToken(String userId, PushTokenRegisterDto dto);
    PageResponseDto<UserNotificationDto> getUserNotifications(String userId, int page, int size);
    UserNotificationDto markNotificationAsRead(String userId, String notificationId);
    NotificationPreferenceDto updatePreferences(String userId, NotificationPreferenceDto dto);
    NotificationPreferenceDto getPreferences(String userId);
}
