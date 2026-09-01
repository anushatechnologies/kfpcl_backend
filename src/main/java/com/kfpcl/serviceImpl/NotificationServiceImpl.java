package com.kfpcl.serviceImpl;

import com.kfpcl.dto.PushTokenRegisterDto;
import com.kfpcl.dto.UserNotificationDto;
import com.kfpcl.dto.NotificationPreferenceDto;
import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.entity.PushToken;
import com.kfpcl.entity.UserNotification;
import com.kfpcl.entity.NotificationPreference;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.PushTokenRepository;
import com.kfpcl.repository.UserNotificationRepository;
import com.kfpcl.repository.NotificationPreferenceRepository;
import com.kfpcl.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final PushTokenRepository pushTokenRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;

    public NotificationServiceImpl(PushTokenRepository pushTokenRepository,
                                   UserNotificationRepository userNotificationRepository,
                                   NotificationPreferenceRepository notificationPreferenceRepository) {
        this.pushTokenRepository = pushTokenRepository;
        this.userNotificationRepository = userNotificationRepository;
        this.notificationPreferenceRepository = notificationPreferenceRepository;
    }

    @Override
    @Transactional
    public PushToken registerToken(String userId, PushTokenRegisterDto dto) {
        Optional<PushToken> existingOpt = pushTokenRepository.findByUserIdAndToken(userId, dto.getToken());
        if (existingOpt.isPresent()) {
            PushToken existing = existingOpt.get();
            if (dto.getDeviceType() != null) {
                existing.setDeviceType(dto.getDeviceType());
            }
            return pushTokenRepository.save(existing);
        }

        PushToken token = PushToken.builder()
                .userId(userId)
                .token(dto.getToken())
                .deviceType(dto.getDeviceType() != null ? dto.getDeviceType() : "FCM")
                .build();

        return pushTokenRepository.save(token);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<UserNotificationDto> getUserNotifications(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<UserNotification> notifs = userNotificationRepository.findByUserId(userId, pageable);

        List<UserNotificationDto> content = notifs.getContent().stream()
                .map(n -> UserNotificationDto.builder()
                        .id(n.getId())
                        .type(n.getType())
                        .title(n.getTitle())
                        .body(n.getBody())
                        .read(n.isRead())
                        .targetPath(n.getTargetPath())
                        .createdAt(n.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return PageResponseDto.<UserNotificationDto>builder()
                .content(content)
                .page(notifs.getNumber())
                .size(notifs.getSize())
                .totalElements(notifs.getTotalElements())
                .totalPages(notifs.getTotalPages())
                .isLast(notifs.isLast())
                .build();
    }

    @Override
    @Transactional
    public UserNotificationDto markNotificationAsRead(String userId, String notificationId) {
        UserNotification notif = userNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notif.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized to modify this notification");
        }

        notif.setRead(true);
        notif = userNotificationRepository.save(notif);

        return UserNotificationDto.builder()
                .id(notif.getId())
                .type(notif.getType())
                .title(notif.getTitle())
                .body(notif.getBody())
                .read(notif.isRead())
                .targetPath(notif.getTargetPath())
                .createdAt(notif.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public NotificationPreferenceDto updatePreferences(String userId, NotificationPreferenceDto dto) {
        NotificationPreference pref = notificationPreferenceRepository.findByUserId(userId)
                .orElse(NotificationPreference.builder()
                        .id(UUID.randomUUID().toString())
                        .userId(userId)
                        .build());

        pref.setNewInquiry(dto.isNewInquiry());
        pref.setOrderUpdates(dto.isOrderUpdates());
        pref.setRfqUpdates(dto.isRfqUpdates());
        pref.setStockAlerts(dto.isStockAlerts());
        pref.setPaymentUpdates(dto.isPaymentUpdates());
        pref.setWhatsappEnabled(dto.isWhatsappEnabled());

        pref = notificationPreferenceRepository.save(pref);
        return mapPrefToDto(pref);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationPreferenceDto getPreferences(String userId) {
        NotificationPreference pref = notificationPreferenceRepository.findByUserId(userId)
                .orElse(NotificationPreference.builder()
                        .newInquiry(true)
                        .orderUpdates(true)
                        .rfqUpdates(true)
                        .stockAlerts(true)
                        .paymentUpdates(true)
                        .whatsappEnabled(false)
                        .build());
        return mapPrefToDto(pref);
    }

    private NotificationPreferenceDto mapPrefToDto(NotificationPreference pref) {
        return NotificationPreferenceDto.builder()
                .newInquiry(pref.isNewInquiry())
                .orderUpdates(pref.isOrderUpdates())
                .rfqUpdates(pref.isRfqUpdates())
                .stockAlerts(pref.isStockAlerts())
                .paymentUpdates(pref.isPaymentUpdates())
                .whatsappEnabled(pref.isWhatsappEnabled())
                .build();
    }
}
