package com.kfpcl.service;

import com.kfpcl.dto.NotificationCreateDto;
import com.kfpcl.dto.NotificationResponseDto;
import com.kfpcl.dto.PageResponseDto;

public interface PlatformNotificationService {

    PageResponseDto<NotificationResponseDto> getNotifications(String status, String audience, int page, int size, String sortBy, String sortDir);

    NotificationResponseDto createNotification(NotificationCreateDto dto);

    NotificationResponseDto getNotificationById(String notificationId);

    NotificationResponseDto sendNotification(String notificationId);

    NotificationResponseDto cancelNotification(String notificationId);
}
