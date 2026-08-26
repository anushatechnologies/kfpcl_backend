package com.kfpcl.user.service;

import com.kfpcl.common.exception.UserNotFoundException;
import com.kfpcl.session.service.SessionService;
import com.kfpcl.user.dto.BuyerProfileResponseDto;
import com.kfpcl.user.dto.BuyerProfileUpdateDto;
import com.kfpcl.user.entity.User;
import com.kfpcl.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SessionService sessionService;

    public UserServiceImpl(UserRepository userRepository, SessionService sessionService) {
        this.userRepository = userRepository;
        this.sessionService = sessionService;
    }

    @Override
    @Transactional(readOnly = true)
    public BuyerProfileResponseDto getBuyerProfile(String userId) {
        User user = userRepository.findByIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new UserNotFoundException("Buyer profile not found with ID: " + userId));

        return toBuyerProfileDto(user);
    }

    @Override
    @Transactional
    public BuyerProfileResponseDto updateBuyerProfile(String userId, BuyerProfileUpdateDto updateDto) {
        User user = userRepository.findByIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new UserNotFoundException("Buyer profile not found with ID: " + userId));

        // Permitted profile updates only
        user.setOwnerName(updateDto.getOwnerName());
        user.setCompanyName(updateDto.getCompanyName());
        user.setEmail(updateDto.getEmail());
        user.setBusinessType(updateDto.getBusinessType());
        user.setAddress(updateDto.getAddress());

        User updatedUser = userRepository.save(user);
        return toBuyerProfileDto(updatedUser);
    }

    @Override
    @Transactional
    public void deleteAccount(String userId, String currentSessionId) {
        User user = userRepository.findByIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new UserNotFoundException("User account not found with ID: " + userId));

        // Soft-delete / deactivate
        user.setActive(false);
        userRepository.save(user);

        // Destroy all server-side sessions
        sessionService.invalidateAllUserSessions(userId);
    }

    private BuyerProfileResponseDto toBuyerProfileDto(User user) {
        return BuyerProfileResponseDto.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .role(user.getRole())
                .ownerName(user.getOwnerName())
                .companyName(user.getCompanyName())
                .email(user.getEmail())
                .businessType(user.getBusinessType())
                .address(user.getAddress())
                .isVerified(user.isVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
