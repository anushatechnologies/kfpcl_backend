package com.kfpcl.admin.service;

import com.kfpcl.common.exception.UserNotFoundException;
import com.kfpcl.kyc.dto.KycApprovalResponseDto;
import com.kfpcl.kyc.dto.KycRejectionRequestDto;
import com.kfpcl.kyc.dto.KycStatusResponseDto;
import com.kfpcl.user.entity.KycStatus;
import com.kfpcl.user.entity.Role;
import com.kfpcl.user.entity.User;
import com.kfpcl.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class AdminKycServiceImpl implements AdminKycService {

    private final UserRepository userRepository;

    public AdminKycServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public KycApprovalResponseDto approveKyc(String supplierId, String adminId) {
        User supplier = userRepository.findByIdAndIsActiveTrue(supplierId)
                .orElseThrow(() -> new UserNotFoundException("Supplier account not found with ID: " + supplierId));

        if (supplier.getRole() != Role.SELLER) {
            throw new IllegalArgumentException("Target user is not a supplier/seller");
        }

        LocalDateTime now = LocalDateTime.now();
        supplier.setKycStatus(KycStatus.APPROVED);
        supplier.setVerified(true);
        supplier.setApprovedAt(now);
        supplier.setApprovedByAdminId(adminId);
        supplier.setRejectionReason(null);

        userRepository.save(supplier);

        return KycApprovalResponseDto.builder()
                .supplierId(supplier.getId())
                .isVerified(true)
                .kycStatus(KycStatus.APPROVED)
                .approvedAt(now)
                .approvedByAdminId(adminId)
                .build();
    }

    @Override
    @Transactional
    public KycStatusResponseDto rejectKyc(String supplierId, KycRejectionRequestDto request, String adminId) {
        User supplier = userRepository.findByIdAndIsActiveTrue(supplierId)
                .orElseThrow(() -> new UserNotFoundException("Supplier account not found with ID: " + supplierId));

        if (supplier.getRole() != Role.SELLER) {
            throw new IllegalArgumentException("Target user is not a supplier/seller");
        }

        LocalDateTime now = LocalDateTime.now();
        supplier.setKycStatus(KycStatus.REJECTED);
        supplier.setVerified(false);
        supplier.setRejectionReason(request.getRejectionReason());
        supplier.setRejectedAt(now);
        supplier.setRejectedByAdminId(adminId);

        userRepository.save(supplier);

        return KycStatusResponseDto.builder()
                .isVerified(false)
                .kycStatus(KycStatus.REJECTED)
                .rejectionReason(supplier.getRejectionReason())
                .build();
    }
}
