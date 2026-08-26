package com.kfpcl.auth.service;

import com.kfpcl.auth.entity.Otp;
import com.kfpcl.auth.entity.OtpPurpose;
import com.kfpcl.auth.repository.OtpRepository;
import com.kfpcl.auth.sms.SmsService;
import com.kfpcl.common.exception.InvalidOtpException;
import com.kfpcl.common.exception.OtpAttemptLimitException;
import com.kfpcl.common.exception.OtpExpiredException;
import com.kfpcl.common.exception.OtpRateLimitException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Slf4j
@Service
public class OtpServiceImpl implements OtpService {

    private final OtpRepository otpRepository;
    private final SmsService smsService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.otp.expiry-minutes:10}")
    private int expiryMinutes;

    @Value("${app.otp.max-send-attempts-per-hour:5}")
    private int maxSendAttemptsPerHour;

    @Value("${app.otp.max-verify-attempts:5}")
    private int maxVerifyAttempts;

    public OtpServiceImpl(OtpRepository otpRepository, SmsService smsService) {
        this.otpRepository = otpRepository;
        this.smsService = smsService;
    }

    @Override
    @Transactional
    public void generateAndSendOtp(String phone, OtpPurpose purpose) {
        // 1. Rate limiting check (max 5 per hour)
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        int recentAttempts = otpRepository.countByPhoneAndCreatedAtAfter(phone, oneHourAgo);
        if (recentAttempts >= maxSendAttemptsPerHour) {
            throw new OtpRateLimitException(String.format(
                    "Maximum OTP request limit (%d per hour) reached for this number. Please try again later.",
                    maxSendAttemptsPerHour
            ));
        }

        // 2. Generate 6-digit OTP
        int otpNumber = 100000 + secureRandom.nextInt(900000);
        String rawOtp = String.valueOf(otpNumber);

        // 3. Hash OTP securely
        String otpHash = hashOtp(phone, rawOtp);

        // 4. Save to Database
        LocalDateTime now = LocalDateTime.now();
        Otp otp = Otp.builder()
                .phone(phone)
                .otpHash(otpHash)
                .purpose(purpose)
                .expiresAt(now.plusMinutes(expiryMinutes))
                .attempts(0)
                .createdAt(now)
                .build();

        otpRepository.save(otp);

        // 5. Send via SMS Service
        smsService.sendOtp(phone, rawOtp);
    }

    @Override
    @Transactional
    public void verifyOtp(String phone, String rawOtp, OtpPurpose purpose) {
        Otp otp = otpRepository.findTopByPhoneAndPurposeOrderByCreatedAtDesc(phone, purpose)
                .orElseThrow(() -> new InvalidOtpException("No OTP found for this phone number. Please request an OTP first."));

        // 1. Check if already verified/used
        if (otp.isVerified()) {
            throw new InvalidOtpException("This OTP has already been used. Please request a new OTP.");
        }

        // 2. Check expiration
        if (otp.isExpired()) {
            throw new OtpExpiredException("OTP has expired. Please request a new OTP.");
        }

        // 3. Check attempt limit
        if (otp.getAttempts() >= maxVerifyAttempts) {
            throw new OtpAttemptLimitException("Maximum verification attempts exceeded. Please request a new OTP.");
        }

        // 4. Increment attempts
        otp.setAttempts(otp.getAttempts() + 1);

        // 5. Verify Hash
        String expectedHash = hashOtp(phone, rawOtp);
        if (!expectedHash.equals(otp.getOtpHash())) {
            otpRepository.save(otp);
            throw new InvalidOtpException("Invalid OTP entered. Please check and try again.");
        }

        // 6. Mark as verified / Invalidate
        otp.setVerifiedAt(LocalDateTime.now());
        otpRepository.save(otp);
    }

    private String hashOtp(String phone, String rawOtp) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String data = phone + ":" + rawOtp + ":kfpcl_secure_otp_pepper";
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
