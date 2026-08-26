package com.kfpcl.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kfpcl.auth.dto.BuyerSignupRequestDto;
import com.kfpcl.auth.dto.CheckPhoneRequestDto;
import com.kfpcl.auth.dto.SendOtpRequestDto;
import com.kfpcl.auth.dto.VerifyOtpRequestDto;
import com.kfpcl.auth.entity.Otp;
import com.kfpcl.auth.entity.OtpPurpose;
import com.kfpcl.auth.repository.OtpRepository;
import com.kfpcl.session.entity.UserSession;
import com.kfpcl.session.repository.UserSessionRepository;
import com.kfpcl.user.dto.BuyerProfileUpdateDto;
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
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class BuyerAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private UserSessionRepository sessionRepository;

    @BeforeEach
    void cleanDatabase() {
        sessionRepository.deleteAll();
        otpRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String hashOtp(String phone, String rawOtp) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String data = phone + ":" + rawOtp + ":kfpcl_secure_otp_pepper";
        return HexFormat.of().formatHex(digest.digest(data.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("Complete Buyer Authentication & Profile Management Flow")
    void testBuyerFullAuthenticationFlow() throws Exception {
        String phone = "9876543210";

        // 1. Check Phone (Unregistered)
        CheckPhoneRequestDto checkPhoneDto = CheckPhoneRequestDto.builder().phone(phone).build();
        mockMvc.perform(post("/api/v1/auth/buyer/check-phone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkPhoneDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.registered", is(false)));

        // 2. Send OTP
        SendOtpRequestDto sendOtpDto = SendOtpRequestDto.builder().phone(phone).build();
        mockMvc.perform(post("/api/v1/auth/buyer/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sendOtpDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // Verify OTP entity created in DB
        Otp savedOtp = otpRepository.findTopByPhoneAndPurposeOrderByCreatedAtDesc(phone, OtpPurpose.BUYER_LOGIN)
                .orElseThrow();
        assertNotNull(savedOtp);

        // Pre-seed known OTP hash for deterministic verification in test
        String rawOtp = "654321";
        savedOtp.setOtpHash(hashOtp(phone, rawOtp));
        otpRepository.save(savedOtp);

        // 3. Verify OTP -> Check HttpOnly Cookie & Session
        VerifyOtpRequestDto verifyDto = VerifyOtpRequestDto.builder().phone(phone).otp(rawOtp).build();
        MvcResult verifyResult = mockMvc.perform(post("/api/v1/auth/buyer/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyDto)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("KFPCL_SESSION_ID"))
                .andExpect(cookie().httpOnly("KFPCL_SESSION_ID", true))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.user.phone", is(phone)))
                .andExpect(jsonPath("$.data.user.role", is("BUYER")))
                .andExpect(jsonPath("$.data.user.isVerified", is(true)))
                .andReturn();

        Cookie sessionCookie = verifyResult.getResponse().getCookie("KFPCL_SESSION_ID");
        assertNotNull(sessionCookie);

        // 4. Update Buyer Profile with Session Cookie
        BuyerProfileUpdateDto updateDto = BuyerProfileUpdateDto.builder()
                .ownerName("Rajesh Kumar")
                .companyName("Apex Infrastructure Pvt Ltd")
                .email("rajesh@apexinfra.com")
                .businessType("Infrastructure")
                .address("Hyderabad, Telangana")
                .build();

        mockMvc.perform(put("/api/v1/buyer/profile")
                        .cookie(sessionCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.ownerName", is("Rajesh Kumar")))
                .andExpect(jsonPath("$.data.companyName", is("Apex Infrastructure Pvt Ltd")));

        // 5. Get Buyer Profile with Session Cookie
        mockMvc.perform(get("/api/v1/buyer/profile")
                        .cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.phone", is(phone)))
                .andExpect(jsonPath("$.data.email", is("rajesh@apexinfra.com")))
                .andExpect(jsonPath("$.data.role", is("BUYER")))
                .andExpect(jsonPath("$.data.isVerified", is(true)));
    }

    @Test
    @DisplayName("Buyer Signup Flow with Complete Business Details")
    void testBuyerSignup() throws Exception {
        BuyerSignupRequestDto signupDto = BuyerSignupRequestDto.builder()
                .ownerName("Suresh Raina")
                .companyName("Green Valley Traders")
                .phone("9811223344")
                .email("suresh@greenvalley.com")
                .businessType("Wholesale Trading")
                .address("Bengaluru, Karnataka")
                .build();

        mockMvc.perform(post("/api/v1/auth/buyer/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupDto)))
                .andExpect(status().isCreated())
                .andExpect(cookie().exists("KFPCL_SESSION_ID"))
                .andExpect(cookie().httpOnly("KFPCL_SESSION_ID", true))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.user.phone", is("9811223344")))
                .andExpect(jsonPath("$.data.user.role", is("BUYER")))
                .andExpect(jsonPath("$.data.user.isVerified", is(true)));
    }
}
