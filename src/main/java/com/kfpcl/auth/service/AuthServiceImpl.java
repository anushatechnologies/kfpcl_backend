package com.kfpcl.auth.service;

import com.kfpcl.auth.dto.AuthResponseDto;
import com.kfpcl.auth.dto.BuyerSignupRequestDto;
import com.kfpcl.auth.dto.CheckPhoneRequestDto;
import com.kfpcl.auth.dto.CheckPhoneResponseDto;
import com.kfpcl.auth.dto.SellerSignupRequestDto;
import com.kfpcl.auth.dto.SendOtpRequestDto;
import com.kfpcl.auth.dto.UserSummaryDto;
import com.kfpcl.auth.dto.VerifyOtpRequestDto;
import com.kfpcl.auth.entity.OtpPurpose;
import com.kfpcl.common.exception.DuplicatePhoneException;
import com.kfpcl.session.entity.UserSession;
import com.kfpcl.session.service.SessionService;
import com.kfpcl.user.entity.KycStatus;
import com.kfpcl.user.entity.Role;
import com.kfpcl.user.entity.User;
import com.kfpcl.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final SessionService sessionService;

    public AuthServiceImpl(UserRepository userRepository,
                           OtpService otpService,
                           SessionService sessionService) {
        this.userRepository = userRepository;
        this.otpService = otpService;
        this.sessionService = sessionService;
    }

    @Override
    @Transactional(readOnly = true)
    public CheckPhoneResponseDto checkBuyerPhone(CheckPhoneRequestDto request) {
        boolean exists = userRepository.existsByPhoneAndRole(request.getPhone(), Role.BUYER);
        return CheckPhoneResponseDto.builder().registered(exists).build();
    }

    @Override
    @Transactional
    public void sendBuyerOtp(SendOtpRequestDto request) {
        otpService.generateAndSendOtp(request.getPhone(), OtpPurpose.BUYER_LOGIN);
    }

    @Override
    @Transactional
    public Pair<AuthResponseDto, UserSession> verifyBuyerOtp(VerifyOtpRequestDto request, String ipAddress, String userAgent) {
        // 1. Verify OTP (throws exception if invalid, expired, or limit exceeded)
        otpService.verifyOtp(request.getPhone(), request.getOtp(), OtpPurpose.BUYER_LOGIN);

        // 2. Find or initialize Buyer user
        Optional<User> existingUserOpt = userRepository.findByPhone(request.getPhone());
        User buyer;
        if (existingUserOpt.isPresent()) {
            buyer = existingUserOpt.get();
        } else {
            buyer = User.builder()
                    .phone(request.getPhone())
                    .role(Role.BUYER)
                    .isVerified(true)
                    .isActive(true)
                    .kycStatus(KycStatus.NOT_APPLICABLE)
                    .build();
            buyer = userRepository.save(buyer);
        }

        // 3. Update last login
        buyer.setLastLoginAt(LocalDateTime.now());
        userRepository.save(buyer);

        // 4. Create Server-Side Session
        UserSession session = sessionService.createSession(buyer.getId(), Role.BUYER, ipAddress, userAgent);

        AuthResponseDto responseDto = AuthResponseDto.builder()
                .user(UserSummaryDto.builder()
                        .id(buyer.getId())
                        .phone(buyer.getPhone())
                        .role(buyer.getRole())
                        .isVerified(buyer.isVerified())
                        .build())
                .build();

        return Pair.of(responseDto, session);
    }

    @Override
    @Transactional
    public Pair<AuthResponseDto, UserSession> signupBuyer(BuyerSignupRequestDto request, String ipAddress, String userAgent) {
        Optional<User> existingOpt = userRepository.findByPhone(request.getPhone());
        User buyer;
        if (existingOpt.isPresent()) {
            buyer = existingOpt.get();
            // If already fully registered with company name and email, reject duplicate signup
            if (buyer.getCompanyName() != null && !buyer.getCompanyName().trim().isEmpty()) {
                throw new DuplicatePhoneException("Phone number is already registered. Please login instead.");
            }
            buyer.setOwnerName(request.getOwnerName());
            buyer.setCompanyName(request.getCompanyName());
            buyer.setEmail(request.getEmail());
            buyer.setBusinessType(request.getBusinessType());
            buyer.setAddress(request.getAddress());
            buyer.setRole(Role.BUYER);
            buyer.setVerified(true);
            buyer.setActive(true);
            buyer.setKycStatus(KycStatus.NOT_APPLICABLE);
        } else {
            buyer = User.builder()
                    .phone(request.getPhone())
                    .ownerName(request.getOwnerName())
                    .companyName(request.getCompanyName())
                    .email(request.getEmail())
                    .businessType(request.getBusinessType())
                    .address(request.getAddress())
                    .role(Role.BUYER)
                    .isVerified(true)
                    .isActive(true)
                    .kycStatus(KycStatus.NOT_APPLICABLE)
                    .build();
        }

        buyer.setLastLoginAt(LocalDateTime.now());
        buyer = userRepository.save(buyer);

        UserSession session = sessionService.createSession(buyer.getId(), Role.BUYER, ipAddress, userAgent);

        AuthResponseDto responseDto = AuthResponseDto.builder()
                .user(UserSummaryDto.builder()
                        .id(buyer.getId())
                        .phone(buyer.getPhone())
                        .role(buyer.getRole())
                        .isVerified(buyer.isVerified())
                        .build())
                .build();

        return Pair.of(responseDto, session);
    }

    @Override
    @Transactional(readOnly = true)
    public CheckPhoneResponseDto checkSupplierPhone(CheckPhoneRequestDto request) {
        boolean exists = userRepository.existsByPhoneAndRole(request.getPhone(), Role.SELLER);
        return CheckPhoneResponseDto.builder().registered(exists).build();
    }

    @Override
    @Transactional
    public void sendSupplierOtp(SendOtpRequestDto request) {
        otpService.generateAndSendOtp(request.getPhone(), OtpPurpose.SELLER_LOGIN);
    }

    @Override
    @Transactional
    public Pair<AuthResponseDto, UserSession> verifySupplierOtp(VerifyOtpRequestDto request, String ipAddress, String userAgent) {
        // 1. Verify OTP
        otpService.verifyOtp(request.getPhone(), request.getOtp(), OtpPurpose.SELLER_LOGIN);

        // 2. Find or initialize Supplier user
        Optional<User> existingUserOpt = userRepository.findByPhone(request.getPhone());
        User seller;
        if (existingUserOpt.isPresent()) {
            seller = existingUserOpt.get();
        } else {
            seller = User.builder()
                    .phone(request.getPhone())
                    .role(Role.SELLER)
                    .isVerified(false)
                    .kycStatus(KycStatus.PENDING)
                    .isActive(true)
                    .build();
            seller = userRepository.save(seller);
        }

        // 3. Update last login
        seller.setLastLoginAt(LocalDateTime.now());
        userRepository.save(seller);

        // 4. Create Server-Side Session with SELLER role
        UserSession session = sessionService.createSession(seller.getId(), Role.SELLER, ipAddress, userAgent);

        AuthResponseDto responseDto = AuthResponseDto.builder()
                .user(UserSummaryDto.builder()
                        .id(seller.getId())
                        .phone(seller.getPhone())
                        .role(seller.getRole())
                        .isVerified(seller.isVerified())
                        .build())
                .build();

        return Pair.of(responseDto, session);
    }

    @Override
    @Transactional
    public Pair<AuthResponseDto, UserSession> signupSupplier(SellerSignupRequestDto request, String ipAddress, String userAgent) {
        Optional<User> existingOpt = userRepository.findByPhone(request.getPhone());
        User seller;
        if (existingOpt.isPresent()) {
            seller = existingOpt.get();
            if (seller.getCompanyName() != null && !seller.getCompanyName().trim().isEmpty()) {
                throw new DuplicatePhoneException("Phone number is already registered. Please login instead.");
            }
            seller.setOwnerName(request.getOwnerName());
            seller.setCompanyName(request.getCompanyName());
            seller.setEmail(request.getEmail());
            seller.setGstNumber(request.getGstNumber().toUpperCase());
            seller.setPanNumber(request.getPanNumber().toUpperCase());
            seller.setBusinessType(request.getBusinessType());
            seller.setAddress(request.getAddress());
            seller.setKycDocUrl(request.getKycDocUrl());
            seller.setPanDocUrl(request.getPanDocUrl());
            seller.setRole(Role.SELLER);
            seller.setVerified(false);
            seller.setKycStatus(KycStatus.PENDING);
            seller.setActive(true);
        } else {
            seller = User.builder()
                    .phone(request.getPhone())
                    .ownerName(request.getOwnerName())
                    .companyName(request.getCompanyName())
                    .email(request.getEmail())
                    .gstNumber(request.getGstNumber().toUpperCase())
                    .panNumber(request.getPanNumber().toUpperCase())
                    .businessType(request.getBusinessType())
                    .address(request.getAddress())
                    .kycDocUrl(request.getKycDocUrl())
                    .panDocUrl(request.getPanDocUrl())
                    .role(Role.SELLER)
                    .isVerified(false)
                    .kycStatus(KycStatus.PENDING)
                    .isActive(true)
                    .build();
        }

        seller.setLastLoginAt(LocalDateTime.now());
        seller = userRepository.save(seller);

        UserSession session = sessionService.createSession(seller.getId(), Role.SELLER, ipAddress, userAgent);

        AuthResponseDto responseDto = AuthResponseDto.builder()
                .user(UserSummaryDto.builder()
                        .id(seller.getId())
                        .phone(seller.getPhone())
                        .role(seller.getRole())
                        .isVerified(seller.isVerified())
                        .build())
                .build();

        return Pair.of(responseDto, session);
    }

    @Override
    @Transactional
    public void logout(String sessionId) {
        sessionService.invalidateSession(sessionId);
    }
}
