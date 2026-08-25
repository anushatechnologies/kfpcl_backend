package com.kfpcl.serviceImpl;

import com.kfpcl.dto.NotificationCreateDto;
import com.kfpcl.dto.NotificationResponseDto;
import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.entity.PlatformNotification;
import com.kfpcl.exception.BusinessValidationException;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.PlatformNotificationRepository;
import com.kfpcl.service.AuditLogService;
import com.kfpcl.service.PlatformNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PlatformNotificationServiceImpl implements PlatformNotificationService {

    private final PlatformNotificationRepository notificationRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<NotificationResponseDto> getNotifications(String status, String audience, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<PlatformNotification> notifPage;
        if (StringUtils.hasText(status)) {
            try {
                PlatformNotification.Status nStatus = PlatformNotification.Status.valueOf(status.trim().toUpperCase());
                notifPage = notificationRepository.findByStatus(nStatus, pageable);
            } catch (IllegalArgumentException e) {
                notifPage = notificationRepository.findAll(pageable);
            }
        } else if (StringUtils.hasText(audience)) {
            notifPage = notificationRepository.findByAudience(audience.trim(), pageable);
        } else {
            notifPage = notificationRepository.findAll(pageable);
        }

        List<NotificationResponseDto> dtoList = notifPage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return PageResponseDto.from(notifPage, dtoList);
    }

    @Override
    public NotificationResponseDto createNotification(NotificationCreateDto dto) {
        PlatformNotification.Status status = PlatformNotification.Status.SCHEDULED;
        if (StringUtils.hasText(dto.getStatus())) {
            try {
                status = PlatformNotification.Status.valueOf(dto.getStatus().trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        LocalDateTime dispatchedAt = status == PlatformNotification.Status.DISPATCHED ? LocalDateTime.now() : null;

        PlatformNotification notification = PlatformNotification.builder()
                .id("notif_" + UUID.randomUUID().toString().substring(0, 8))
                .title(dto.getTitle().trim())
                .message(dto.getMessage().trim())
                .audience(dto.getAudience().trim())
                .channels(StringUtils.hasText(dto.getChannels()) ? dto.getChannels() : "IN_APP")
                .scheduledAt(dto.getScheduledAt() != null ? dto.getScheduledAt() : LocalDateTime.now())
                .status(status)
                .dispatchedAt(dispatchedAt)
                .build();

        PlatformNotification saved = notificationRepository.save(notification);
        auditLogService.logAction("admin", "ROLE_ADMIN", "CREATE_NOTIFICATION", "NOTIFICATION", saved.getId(), null, saved.getTitle(), null, null);

        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponseDto getNotificationById(String notificationId) {
        PlatformNotification notif = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "notificationId", notificationId));
        return mapToDto(notif);
    }

    @Override
    public NotificationResponseDto sendNotification(String notificationId) {
        PlatformNotification notif = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "notificationId", notificationId));

        if (notif.getStatus() == PlatformNotification.Status.DISPATCHED) {
            throw new BusinessValidationException("Notification has already been dispatched");
        }

        if (notif.getStatus() == PlatformNotification.Status.CANCELLED) {
            throw new BusinessValidationException("Cannot dispatch a cancelled notification");
        }

        notif.setStatus(PlatformNotification.Status.DISPATCHED);
        notif.setDispatchedAt(LocalDateTime.now());
        PlatformNotification saved = notificationRepository.save(notif);

        auditLogService.logAction("admin", "ROLE_ADMIN", "DISPATCH_NOTIFICATION", "NOTIFICATION", notificationId, "SCHEDULED", "DISPATCHED", null, null);

        return mapToDto(saved);
    }

    @Override
    public NotificationResponseDto cancelNotification(String notificationId) {
        PlatformNotification notif = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "notificationId", notificationId));

        if (notif.getStatus() == PlatformNotification.Status.DISPATCHED) {
            throw new BusinessValidationException("Cannot cancel an already dispatched notification");
        }

        notif.setStatus(PlatformNotification.Status.CANCELLED);
        PlatformNotification saved = notificationRepository.save(notif);

        auditLogService.logAction("admin", "ROLE_ADMIN", "CANCEL_NOTIFICATION", "NOTIFICATION", notificationId, "SCHEDULED", "CANCELLED", null, null);

        return mapToDto(saved);
    }

    private NotificationResponseDto mapToDto(PlatformNotification n) {
        return NotificationResponseDto.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .audience(n.getAudience())
                .channels(n.getChannels())
                .scheduledAt(n.getScheduledAt())
                .status(n.getStatus().name())
                .dispatchedAt(n.getDispatchedAt())
                .createdAt(n.getCreatedAt())
                .updatedAt(n.getUpdatedAt())
                .build();
    }
}
