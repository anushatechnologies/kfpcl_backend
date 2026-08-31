package com.kfpcl.serviceImpl;

import com.kfpcl.dto.PushTokenRegisterDto;
import com.kfpcl.entity.PushToken;
import com.kfpcl.repository.PushTokenRepository;
import com.kfpcl.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final PushTokenRepository pushTokenRepository;

    public NotificationServiceImpl(PushTokenRepository pushTokenRepository) {
        this.pushTokenRepository = pushTokenRepository;
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
}
