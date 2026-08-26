package com.kfpcl.auth.service;

import com.kfpcl.auth.dto.AuthResponseDto;
import com.kfpcl.auth.dto.BuyerSignupRequestDto;
import com.kfpcl.auth.dto.CheckPhoneRequestDto;
import com.kfpcl.auth.dto.CheckPhoneResponseDto;
import com.kfpcl.auth.dto.SellerSignupRequestDto;
import com.kfpcl.auth.dto.VerifyOtpRequestDto;
import com.kfpcl.auth.entity.OtpPurpose;
import com.kfpcl.common.exception.DuplicatePhoneException;
import com.kfpcl.session.entity.UserSession;
import com.kfpcl.session.service.SessionService;
import com.kfpcl.user.entity.KycStatus;
import com.kfpcl.user.entity.Role;
import com.kfpcl.user.entity.User;
import com.kfpcl.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OtpService otpService;

    @Mock
    private SessionService sessionService;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userRepository, otpService, sessionService);
    }

    @Test
    @DisplayName("Should check phone registration status correctly")
    void testCheckBuyerPhone() {
        when(userRepository.existsByPhoneAndRole("9876543210", Role.BUYER)).thenReturn(true);

        CheckPhoneResponseDto result = authService.checkBuyerPhone(
                CheckPhoneRequestDto.builder().phone("9876543210").build()
        );

        assertTrue(result.isRegistered());
    }

    @Test
    @DisplayName("Buyer verify OTP should set isVerified = true and create session")
    void testVerifyBuyerOtp_Success() {
        VerifyOtpRequestDto request = VerifyOtpRequestDto.builder()
                .phone("9876543210")
                .otp("123456")
                .build();

        doNothing().when(otpService).verifyOtp("9876543210", "123456", OtpPurpose.BUYER_LOGIN);
        when(userRepository.findByPhone("9876543210")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            if (u.getId() == null) u.setId("usr_buyer1");
            return u;
        });

        UserSession mockSession = UserSession.builder()
                .sessionId("sess_buyer123")
                .userId("usr_buyer1")
                .role(Role.BUYER)
                .isActive(true)
                .build();

        when(sessionService.createSession(eq("usr_buyer1"), eq(Role.BUYER), any(), any()))
                .thenReturn(mockSession);

        Pair<AuthResponseDto, UserSession> result = authService.verifyBuyerOtp(request, "127.0.0.1", "JUnit");

        assertNotNull(result);
        assertEquals(Role.BUYER, result.getFirst().getUser().getRole());
        assertTrue(result.getFirst().getUser().isVerified());
        assertEquals("sess_buyer123", result.getSecond().getSessionId());
    }

    @Test
    @DisplayName("Seller verify OTP should keep isVerified = false and kycStatus = PENDING")
    void testVerifySupplierOtp_Success() {
        VerifyOtpRequestDto request = VerifyOtpRequestDto.builder()
                .phone("9822011223")
                .otp("123456")
                .build();

        doNothing().when(otpService).verifyOtp("9822011223", "123456", OtpPurpose.SELLER_LOGIN);
        when(userRepository.findByPhone("9822011223")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            if (u.getId() == null) u.setId("usr_seller1");
            return u;
        });

        UserSession mockSession = UserSession.builder()
                .sessionId("sess_seller123")
                .userId("usr_seller1")
                .role(Role.SELLER)
                .isActive(true)
                .build();

        when(sessionService.createSession(eq("usr_seller1"), eq(Role.SELLER), any(), any()))
                .thenReturn(mockSession);

        Pair<AuthResponseDto, UserSession> result = authService.verifySupplierOtp(request, "127.0.0.1", "JUnit");

        assertNotNull(result);
        assertEquals(Role.SELLER, result.getFirst().getUser().getRole());
        assertFalse(result.getFirst().getUser().isVerified()); // Must NOT be automatically verified!
        assertEquals("sess_seller123", result.getSecond().getSessionId());
    }

    @Test
    @DisplayName("Buyer signup should fail if phone is already registered with completed profile")
    void testBuyerSignup_DuplicatePhone() {
        BuyerSignupRequestDto request = BuyerSignupRequestDto.builder()
                .ownerName("Rajesh Kumar")
                .companyName("Apex Infra")
                .phone("9876543210")
                .email("rajesh@apex.com")
                .businessType("Construction")
                .address("Hyderabad")
                .build();

        User existingUser = User.builder()
                .id("usr_existing")
                .phone("9876543210")
                .companyName("Existing Company")
                .role(Role.BUYER)
                .build();

        when(userRepository.findByPhone("9876543210")).thenReturn(Optional.of(existingUser));

        assertThrows(DuplicatePhoneException.class, () ->
                authService.signupBuyer(request, "127.0.0.1", "JUnit")
        );
    }

    @Test
    @DisplayName("Supplier signup should set initial state to isVerified = false, kycStatus = PENDING")
    void testSupplierSignup_Success() {
        SellerSignupRequestDto request = SellerSignupRequestDto.builder()
                .ownerName("Vikram Singhania")
                .companyName("Apex Machinery")
                .phone("9822011223")
                .email("sales@apex.com")
                .gstNumber("27AABCA1234F1Z1")
                .panNumber("AABCA1234F")
                .businessType("Manufacturer")
                .address("Pune")
                .kycDocUrl("https://storage/gst.pdf")
                .panDocUrl("https://storage/pan.jpg")
                .build();

        when(userRepository.findByPhone("9822011223")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            if (u.getId() == null) u.setId("usr_seller1");
            return u;
        });

        UserSession mockSession = UserSession.builder()
                .sessionId("sess_seller_signup")
                .userId("usr_seller1")
                .role(Role.SELLER)
                .isActive(true)
                .build();

        when(sessionService.createSession(eq("usr_seller1"), eq(Role.SELLER), any(), any()))
                .thenReturn(mockSession);

        Pair<AuthResponseDto, UserSession> result = authService.signupSupplier(request, "127.0.0.1", "JUnit");

        assertNotNull(result);
        assertEquals(Role.SELLER, result.getFirst().getUser().getRole());
        assertFalse(result.getFirst().getUser().isVerified());
    }

    @Test
    @DisplayName("Logout should invalidate session")
    void testLogout() {
        authService.logout("sess_123");
        verify(sessionService).invalidateSession("sess_123");
    }
}
