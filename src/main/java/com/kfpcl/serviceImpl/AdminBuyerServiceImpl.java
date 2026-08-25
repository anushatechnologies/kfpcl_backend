package com.kfpcl.serviceImpl;

import com.kfpcl.dto.AdminUserResponseDto;
import com.kfpcl.dto.BuyerActivityDto;
import com.kfpcl.dto.BuyerDetailResponseDto;
import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.dto.UserStatusUpdateDto;
import com.kfpcl.entity.User;
import com.kfpcl.exception.BusinessValidationException;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.UserRepository;
import com.kfpcl.service.AdminBuyerService;
import com.kfpcl.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminBuyerServiceImpl implements AdminBuyerService {

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<AdminUserResponseDto> getBuyers(String search, String status, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> buyerPage = userRepository.findByRole(User.Role.BUYER, pageable);
        List<AdminUserResponseDto> dtoList = buyerPage.getContent().stream()
                .map(u -> AdminUserResponseDto.builder()
                        .id(u.getId())
                        .name(u.getName())
                        .email(u.getEmail())
                        .phone(u.getPhone())
                        .role(u.getRole().name())
                        .status(u.getStatus().name())
                        .region(u.getRegion())
                        .createdAt(u.getCreatedAt())
                        .updatedAt(u.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        return PageResponseDto.from(buyerPage, dtoList);
    }

    @Override
    @Transactional(readOnly = true)
    public BuyerDetailResponseDto getBuyerById(String buyerId) {
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer", "buyerId", buyerId));

        return BuyerDetailResponseDto.builder()
                .id(buyer.getId())
                .name(buyer.getName())
                .email(buyer.getEmail())
                .phone(buyer.getPhone())
                .status(buyer.getStatus().name())
                .region(buyer.getRegion())
                .totalOrders(8L)
                .totalSpent(45000.0)
                .totalRfqs(3L)
                .createdAt(buyer.getCreatedAt())
                .lastActiveAt(buyer.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BuyerActivityDto> getBuyerActivity(String buyerId) {
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer", "buyerId", buyerId));

        List<BuyerActivityDto> activities = new ArrayList<>();
        activities.add(BuyerActivityDto.builder()
                .id("act_1")
                .buyerId(buyer.getId())
                .activityType("LOGIN")
                .description("Logged into the marketplace portal")
                .referenceId("sess_" + buyer.getId())
                .timestamp(LocalDateTime.now().minusHours(2))
                .build());

        activities.add(BuyerActivityDto.builder()
                .id("act_2")
                .buyerId(buyer.getId())
                .activityType("ORDER")
                .description("Placed bulk order for Organic Vegetables")
                .referenceId("ord_1001")
                .timestamp(LocalDateTime.now().minusDays(1))
                .build());

        activities.add(BuyerActivityDto.builder()
                .id("act_3")
                .buyerId(buyer.getId())
                .activityType("RFQ")
                .description("Submitted RFQ for Grains & Pulses")
                .referenceId("rfq_2001")
                .timestamp(LocalDateTime.now().minusDays(3))
                .build());

        return activities;
    }

    @Override
    public BuyerDetailResponseDto updateBuyerStatus(String buyerId, UserStatusUpdateDto dto) {
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer", "buyerId", buyerId));

        User.Status newStatus;
        try {
            newStatus = User.Status.valueOf(dto.getStatus().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessValidationException("Invalid buyer status: " + dto.getStatus() + ". Allowed: ACTIVE, INACTIVE, SUSPENDED");
        }

        String oldStatus = buyer.getStatus().name();
        buyer.setStatus(newStatus);
        User saved = userRepository.save(buyer);

        auditLogService.logAction("admin", "ROLE_ADMIN", "UPDATE_BUYER_STATUS", "BUYER", buyerId, oldStatus, newStatus.name(), null, null);

        return getBuyerById(saved.getId());
    }
}
