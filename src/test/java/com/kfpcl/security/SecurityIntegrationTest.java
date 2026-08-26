package com.kfpcl.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kfpcl.auth.dto.BuyerSignupRequestDto;
import com.kfpcl.auth.dto.SellerSignupRequestDto;
import com.kfpcl.auth.dto.VerifyOtpRequestDto;
import com.kfpcl.auth.entity.Otp;
import com.kfpcl.auth.entity.OtpPurpose;
import com.kfpcl.auth.repository.OtpRepository;
import com.kfpcl.session.entity.UserSession;
import com.kfpcl.session.repository.UserSessionRepository;
import com.kfpcl.user.entity.Role;
import com.kfpcl.user.entity.User;
import com.kfpcl.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSessionRepository sessionRepository;

    @Autowired
    private OtpRepository otpRepository;

    private User buyerUser;
    private User sellerUser;
    private Cookie buyerCookie;
    private Cookie sellerCookie;

    @BeforeEach
    void setUp() {
        sessionRepository.deleteAll();
        otpRepository.deleteAll();
        userRepository.deleteAll();

        // Create Buyer
        buyerUser = User.builder()
                .id("usr_buyer_sec")
                .phone("9876543210")
                .companyName("Buyer Corp")
                .role(Role.BUYER)
                .isVerified(true)
                .isActive(true)
                .build();
        userRepository.save(buyerUser);

        UserSession buyerSession = UserSession.builder()
                .sessionId("sess_buyer_sec")
                .userId(buyerUser.getId())
                .role(Role.BUYER)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(8))
                .lastAccessedAt(LocalDateTime.now())
                .isActive(true)
                .build();
        sessionRepository.save(buyerSession);
        buyerCookie = new Cookie("KFPCL_SESSION_ID", "sess_buyer_sec");

        // Create Seller
        sellerUser = User.builder()
                .id("usr_seller_sec")
                .phone("9822011223")
                .companyName("Seller Corp")
                .role(Role.SELLER)
                .isVerified(false)
                .isActive(true)
                .build();
        userRepository.save(sellerUser);

        UserSession sellerSession = UserSession.builder()
                .sessionId("sess_seller_sec")
                .userId(sellerUser.getId())
                .role(Role.SELLER)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(8))
                .lastAccessedAt(LocalDateTime.now())
                .isActive(true)
                .build();
        sessionRepository.save(sellerSession);
        sellerCookie = new Cookie("KFPCL_SESSION_ID", "sess_seller_sec");
    }

    @Test
    @DisplayName("Protected API should return 401 UNAUTHORIZED when session cookie is missing")
    void testMissingSessionReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/buyer/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is("UNAUTHORIZED")));
    }

    @Test
    @DisplayName("Protected API should return 401 UNAUTHORIZED when session is invalid or non-existent")
    void testInvalidSessionReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/buyer/profile")
                        .cookie(new Cookie("KFPCL_SESSION_ID", "sess_invalid_nonexistent")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is("INVALID_SESSION")));
    }

    @Test
    @DisplayName("Protected API should return 401 UNAUTHORIZED when session is expired")
    void testExpiredSessionReturns401() throws Exception {
        UserSession expiredSession = UserSession.builder()
                .sessionId("sess_expired_sec")
                .userId(buyerUser.getId())
                .role(Role.BUYER)
                .createdAt(LocalDateTime.now().minusHours(10))
                .expiresAt(LocalDateTime.now().minusMinutes(5))
                .lastAccessedAt(LocalDateTime.now().minusHours(5))
                .isActive(true)
                .build();
        sessionRepository.save(expiredSession);

        mockMvc.perform(get("/api/v1/buyer/profile")
                        .cookie(new Cookie("KFPCL_SESSION_ID", "sess_expired_sec")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is("SESSION_EXPIRED")));
    }

    @Test
    @DisplayName("Buyer attempting to access Seller API should receive 403 FORBIDDEN")
    void testBuyerAccessingSellerApiReturns403() throws Exception {
        mockMvc.perform(get("/api/v1/seller/kyc-status")
                        .cookie(buyerCookie))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is("FORBIDDEN")));
    }

    @Test
    @DisplayName("Buyer attempting to access Admin API should receive 403 FORBIDDEN")
    void testBuyerAccessingAdminApiReturns403() throws Exception {
        mockMvc.perform(put("/api/v1/admin/suppliers/" + sellerUser.getId() + "/approve-kyc")
                        .cookie(buyerCookie))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is("FORBIDDEN")));
    }

    @Test
    @DisplayName("Seller attempting to access Admin API should receive 403 FORBIDDEN")
    void testSellerAccessingAdminApiReturns403() throws Exception {
        mockMvc.perform(put("/api/v1/admin/suppliers/" + sellerUser.getId() + "/approve-kyc")
                        .cookie(sellerCookie))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is("FORBIDDEN")));
    }

    @Test
    @DisplayName("Invalid OTP verification should return 400 BAD REQUEST")
    void testInvalidOtpReturns400() throws Exception {
        VerifyOtpRequestDto verifyDto = VerifyOtpRequestDto.builder()
                .phone("9876543210")
                .otp("000000")
                .build();

        mockMvc.perform(post("/api/v1/auth/buyer/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is("INVALID_OTP")));
    }

    @Test
    @DisplayName("Duplicate phone signup should return 409 CONFLICT")
    void testDuplicatePhoneSignupReturns409() throws Exception {
        BuyerSignupRequestDto duplicateSignup = BuyerSignupRequestDto.builder()
                .ownerName("Another Buyer")
                .companyName("Another Company")
                .phone("9876543210") // already exists
                .email("another@email.com")
                .businessType("Retail")
                .address("Mumbai")
                .build();

        mockMvc.perform(post("/api/v1/auth/buyer/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateSignup)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is("DUPLICATE_PHONE")));
    }

    @Test
    @DisplayName("Supplier signup with invalid GSTIN should return 400 BAD REQUEST")
    void testInvalidGstinReturns400() throws Exception {
        SellerSignupRequestDto invalidGstinDto = SellerSignupRequestDto.builder()
                .ownerName("Test Seller")
                .companyName("Test Company")
                .phone("9833445566")
                .email("test@seller.com")
                .gstNumber("INVALID_GSTIN") // invalid format
                .panNumber("AABCA1234F")
                .businessType("Manufacturer")
                .address("Delhi")
                .kycDocUrl("https://storage/doc.pdf")
                .panDocUrl("https://storage/pan.jpg")
                .build();

        mockMvc.perform(post("/api/v1/auth/supplier/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidGstinDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is("VALIDATION_FAILED")));
    }

    @Test
    @DisplayName("Supplier signup with invalid PAN should return 400 BAD REQUEST")
    void testInvalidPanReturns400() throws Exception {
        SellerSignupRequestDto invalidPanDto = SellerSignupRequestDto.builder()
                .ownerName("Test Seller")
                .companyName("Test Company")
                .phone("9833445566")
                .email("test@seller.com")
                .gstNumber("27AABCA1234F1Z1")
                .panNumber("INVALID_PAN") // invalid format
                .businessType("Manufacturer")
                .address("Delhi")
                .kycDocUrl("https://storage/doc.pdf")
                .panDocUrl("https://storage/pan.jpg")
                .build();

        mockMvc.perform(post("/api/v1/auth/supplier/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPanDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is("VALIDATION_FAILED")));
    }

    @Test
    @DisplayName("Account Deletion should deactivate account and invalidate all sessions")
    void testAccountDeletionFlow() throws Exception {
        // Delete account
        mockMvc.perform(delete("/api/v1/user/account")
                        .cookie(buyerCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(cookie().maxAge("KFPCL_SESSION_ID", 0)); // Cookie cleared

        // Subsequent access with deleted account/session should fail with 401
        mockMvc.perform(get("/api/v1/buyer/profile")
                        .cookie(buyerCookie))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Logout should invalidate session and clear cookie")
    void testLogoutFlow() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(buyerCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(cookie().maxAge("KFPCL_SESSION_ID", 0));

        // Subsequent access should fail with 401
        mockMvc.perform(get("/api/v1/buyer/profile")
                        .cookie(buyerCookie))
                .andExpect(status().isUnauthorized());
    }
}
