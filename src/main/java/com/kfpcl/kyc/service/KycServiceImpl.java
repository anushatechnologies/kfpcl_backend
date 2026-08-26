package com.kfpcl.kyc.service;

import com.kfpcl.common.exception.KycAlreadyApprovedException;
import com.kfpcl.common.exception.UserNotFoundException;
import com.kfpcl.kyc.dto.KycResubmitRequestDto;
import com.kfpcl.kyc.dto.KycStatusResponseDto;
import com.kfpcl.user.entity.KycStatus;
import com.kfpcl.user.entity.User;
import com.kfpcl.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class KycServiceImpl implements KycService {

    private final UserRepository userRepository;

    public KycServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public KycStatusResponseDto getKycStatus(String sellerId) {
        User user = userRepository.findByIdAndIsActiveTrue(sellerId)
                .orElseThrow(() -> new UserNotFoundException("Seller account not found with ID: " + sellerId));

        return KycStatusResponseDto.builder()
                .isVerified(user.isVerified())
                .kycStatus(user.getKycStatus() != null ? user.getKycStatus() : KycStatus.PENDING)
                .rejectionReason(user.getRejectionReason())
                .build();
    }

    @Override
    @Transactional
    public KycStatusResponseDto resubmitKyc(String sellerId, KycResubmitRequestDto request) {
        User user = userRepository.findByIdAndIsActiveTrue(sellerId)
                .orElseThrow(() -> new UserNotFoundException("Seller account not found with ID: " + sellerId));

        if (user.isVerified() && user.getKycStatus() == KycStatus.APPROVED) {
            throw new KycAlreadyApprovedException("Seller KYC is already approved and verified. Resubmission is not permitted.");
        }

        user.setGstNumber(request.getGstNumber().toUpperCase());
        user.setPanNumber(request.getPanNumber().toUpperCase());
        user.setKycDocUrl(request.getKycDocUrl());
        user.setPanDocUrl(request.getPanDocUrl());
        user.setKycStatus(KycStatus.SUBMITTED);
        user.setVerified(false);
        user.setRejectionReason(null);

        User saved = userRepository.save(user);

        return KycStatusResponseDto.builder()
                .isVerified(saved.isVerified())
                .kycStatus(saved.getKycStatus())
                .rejectionReason(saved.getRejectionReason())
                .build();
    }
}
