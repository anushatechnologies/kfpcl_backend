package com.kfpcl.auth.service;

import com.kfpcl.auth.entity.Otp;
import com.kfpcl.auth.entity.OtpPurpose;
import com.kfpcl.auth.repository.OtpRepository;
import com.kfpcl.auth.sms.SmsService;
import com.kfpcl.common.exception.InvalidOtpException;
import com.kfpcl.common.exception.OtpAttemptLimitException;
import com.kfpcl.common.exception.OtpExpiredException;
import com.kfpcl.common.exception.OtpRateLimitException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private OtpRepository otpRepository;

    @Mock
    private SmsService smsService;

    private OtpServiceImpl otpService;

    private final String phone = "9876543210";

    @BeforeEach
    void setUp() {
        otpService = new OtpServiceImpl(otpRepository, smsService);
        ReflectionTestUtils.setField(otpService, "expiryMinutes", 10);
        ReflectionTestUtils.setField(otpService, "maxSendAttemptsPerHour", 5);
        ReflectionTestUtils.setField(otpService, "maxVerifyAttempts", 5);
    }

    @Test
    @DisplayName("Should generate and send 6-digit OTP when within rate limits")
    void testGenerateAndSendOtp_Success() {
        when(otpRepository.countByPhoneAndCreatedAtAfter(eq(phone), any(LocalDateTime.class))).thenReturn(2);

        otpService.generateAndSendOtp(phone, OtpPurpose.BUYER_LOGIN);

        ArgumentCaptor<Otp> otpCaptor = ArgumentCaptor.forClass(Otp.class);
        verify(otpRepository).save(otpCaptor.capture());

        Otp savedOtp = otpCaptor.getValue();
        assertEquals(phone, savedOtp.getPhone());
        assertEquals(OtpPurpose.BUYER_LOGIN, savedOtp.getPurpose());
        assertNotNull(savedOtp.getOtpHash());
        assertEquals(0, savedOtp.getAttempts());

        ArgumentCaptor<String> smsOtpCaptor = ArgumentCaptor.forClass(String.class);
        verify(smsService).sendOtp(eq(phone), smsOtpCaptor.capture());
        String generatedOtp = smsOtpCaptor.getValue();
        assertNotNull(generatedOtp);
        assertEquals(6, generatedOtp.length());
        assertTrue(generatedOtp.matches("^\\d{6}$"));
    }

    @Test
    @DisplayName("Should throw OtpRateLimitException when hourly send limit exceeded")
    void testGenerateAndSendOtp_RateLimitExceeded() {
        when(otpRepository.countByPhoneAndCreatedAtAfter(eq(phone), any(LocalDateTime.class))).thenReturn(5);

        assertThrows(OtpRateLimitException.class, () ->
                otpService.generateAndSendOtp(phone, OtpPurpose.BUYER_LOGIN)
        );
    }

    @Test
    @DisplayName("Should verify OTP successfully and invalidate it")
    void testVerifyOtp_Success() {
        // Generate an OTP first to get valid hash
        when(otpRepository.countByPhoneAndCreatedAtAfter(eq(phone), any(LocalDateTime.class))).thenReturn(0);
        otpService.generateAndSendOtp(phone, OtpPurpose.BUYER_LOGIN);

        ArgumentCaptor<String> smsOtpCaptor = ArgumentCaptor.forClass(String.class);
        verify(smsService).sendOtp(eq(phone), smsOtpCaptor.capture());
        String rawOtp = smsOtpCaptor.getValue();

        ArgumentCaptor<Otp> otpCaptor = ArgumentCaptor.forClass(Otp.class);
        verify(otpRepository).save(otpCaptor.capture());
        Otp storedOtp = otpCaptor.getValue();

        when(otpRepository.findTopByPhoneAndPurposeOrderByCreatedAtDesc(phone, OtpPurpose.BUYER_LOGIN))
                .thenReturn(Optional.of(storedOtp));

        otpService.verifyOtp(phone, rawOtp, OtpPurpose.BUYER_LOGIN);

        assertNotNull(storedOtp.getVerifiedAt());
        assertTrue(storedOtp.isVerified());
    }

    @Test
    @DisplayName("Should throw InvalidOtpException when OTP is incorrect")
    void testVerifyOtp_IncorrectOtp() {
        Otp storedOtp = Otp.builder()
                .phone(phone)
                .otpHash("dummyhash123")
                .purpose(OtpPurpose.BUYER_LOGIN)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .attempts(0)
                .createdAt(LocalDateTime.now())
                .build();

        when(otpRepository.findTopByPhoneAndPurposeOrderByCreatedAtDesc(phone, OtpPurpose.BUYER_LOGIN))
                .thenReturn(Optional.of(storedOtp));

        assertThrows(InvalidOtpException.class, () ->
                otpService.verifyOtp(phone, "999999", OtpPurpose.BUYER_LOGIN)
        );

        assertEquals(1, storedOtp.getAttempts());
    }

    @Test
    @DisplayName("Should throw OtpExpiredException when OTP has expired")
    void testVerifyOtp_Expired() {
        Otp expiredOtp = Otp.builder()
                .phone(phone)
                .otpHash("dummyhash")
                .purpose(OtpPurpose.BUYER_LOGIN)
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .attempts(0)
                .createdAt(LocalDateTime.now().minusMinutes(11))
                .build();

        when(otpRepository.findTopByPhoneAndPurposeOrderByCreatedAtDesc(phone, OtpPurpose.BUYER_LOGIN))
                .thenReturn(Optional.of(expiredOtp));

        assertThrows(OtpExpiredException.class, () ->
                otpService.verifyOtp(phone, "123456", OtpPurpose.BUYER_LOGIN)
        );
    }

    @Test
    @DisplayName("Should throw InvalidOtpException when attempting to reuse already verified OTP")
    void testVerifyOtp_AlreadyUsed() {
        Otp usedOtp = Otp.builder()
                .phone(phone)
                .otpHash("dummyhash")
                .purpose(OtpPurpose.BUYER_LOGIN)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .verifiedAt(LocalDateTime.now().minusMinutes(1))
                .attempts(1)
                .createdAt(LocalDateTime.now().minusMinutes(2))
                .build();

        when(otpRepository.findTopByPhoneAndPurposeOrderByCreatedAtDesc(phone, OtpPurpose.BUYER_LOGIN))
                .thenReturn(Optional.of(usedOtp));

        assertThrows(InvalidOtpException.class, () ->
                otpService.verifyOtp(phone, "123456", OtpPurpose.BUYER_LOGIN)
        );
    }

    @Test
    @DisplayName("Should throw OtpAttemptLimitException when verification attempts exceed limit")
    void testVerifyOtp_AttemptLimitExceeded() {
        Otp maxAttemptsOtp = Otp.builder()
                .phone(phone)
                .otpHash("dummyhash")
                .purpose(OtpPurpose.BUYER_LOGIN)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .attempts(5)
                .createdAt(LocalDateTime.now().minusMinutes(2))
                .build();

        when(otpRepository.findTopByPhoneAndPurposeOrderByCreatedAtDesc(phone, OtpPurpose.BUYER_LOGIN))
                .thenReturn(Optional.of(maxAttemptsOtp));

        assertThrows(OtpAttemptLimitException.class, () ->
                otpService.verifyOtp(phone, "123456", OtpPurpose.BUYER_LOGIN)
        );
    }
}
