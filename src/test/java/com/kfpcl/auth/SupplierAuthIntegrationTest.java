package com.kfpcl.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kfpcl.auth.dto.CheckPhoneRequestDto;
import com.kfpcl.auth.dto.SellerSignupRequestDto;
import com.kfpcl.auth.dto.SendOtpRequestDto;
import com.kfpcl.auth.dto.VerifyOtpRequestDto;
import com.kfpcl.auth.entity.Otp;
import com.kfpcl.auth.entity.OtpPurpose;
import com.kfpcl.auth.repository.OtpRepository;
import com.kfpcl.kyc.dto.KycResubmitRequestDto;
import com.kfpcl.session.repository.UserSessionRepository;
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
import java.util.HexFormat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class SupplierAuthIntegrationTest {

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
    @DisplayName("Supplier Signup with GST/PAN and KYC verification status check")
    void testSupplierSignupAndKycFlow() throws Exception {
        SellerSignupRequestDto signupDto = SellerSignupRequestDto.builder()
                .ownerName("Vikram Singhania")
                .companyName("Apex Machinery Exports")
                .phone("9822011223")
                .email("sales@apexmachinery.com")
                .gstNumber("27AABCA1234F1Z1")
                .panNumber("AABCA1234F")
                .businessType("Manufacturer & Exporter")
                .address("Plot 12, Bhosari Industrial Estate, Pune, Maharashtra")
                .kycDocUrl("https://storage.kfpcl.com/uploads/gst_cert.pdf")
                .panDocUrl("https://storage.kfpcl.com/uploads/pan_card.jpg")
                .build();

        // 1. Supplier Signup
        MvcResult signupResult = mockMvc.perform(post("/api/v1/auth/supplier/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupDto)))
                .andExpect(status().isCreated())
                .andExpect(cookie().exists("KFPCL_SESSION_ID"))
                .andExpect(cookie().httpOnly("KFPCL_SESSION_ID", true))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.user.phone", is("9822011223")))
                .andExpect(jsonPath("$.data.user.role", is("SELLER")))
                .andExpect(jsonPath("$.data.user.isVerified", is(false))) // Initial state MUST be false!
                .andReturn();

        Cookie sessionCookie = signupResult.getResponse().getCookie("KFPCL_SESSION_ID");
        assertNotNull(sessionCookie);

        // 2. Get Seller KYC Status (Pending)
        mockMvc.perform(get("/api/v1/seller/kyc-status")
                        .cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.isVerified", is(false)))
                .andExpect(jsonPath("$.data.kycStatus", is("PENDING")))
                .andExpect(jsonPath("$.data.rejectionReason", nullValue()));

        // 3. Resubmit KYC Documents
        KycResubmitRequestDto resubmitDto = KycResubmitRequestDto.builder()
                .gstNumber("27AABCA1234F1Z1")
                .panNumber("AABCA1234F")
                .kycDocUrl("https://storage.kfpcl.com/uploads/new_gst.pdf")
                .panDocUrl("https://storage.kfpcl.com/uploads/new_pan.jpg")
                .build();

        mockMvc.perform(post("/api/v1/seller/resubmit-kyc")
                        .cookie(sessionCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resubmitDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.kycStatus", is("SUBMITTED")))
                .andExpect(jsonPath("$.data.isVerified", is(false)));
    }

    @Test
    @DisplayName("Supplier Verify OTP creates unverified seller session")
    void testSupplierOtpLogin() throws Exception {
        String phone = "9822011223";

        // Send OTP
        SendOtpRequestDto sendOtpDto = SendOtpRequestDto.builder().phone(phone).build();
        mockMvc.perform(post("/api/v1/auth/supplier/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sendOtpDto)))
                .andExpect(status().isOk());

        Otp savedOtp = otpRepository.findTopByPhoneAndPurposeOrderByCreatedAtDesc(phone, OtpPurpose.SELLER_LOGIN)
                .orElseThrow();
        String rawOtp = "123456";
        savedOtp.setOtpHash(hashOtp(phone, rawOtp));
        otpRepository.save(savedOtp);

        // Verify OTP
        VerifyOtpRequestDto verifyDto = VerifyOtpRequestDto.builder().phone(phone).otp(rawOtp).build();
        mockMvc.perform(post("/api/v1/auth/supplier/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyDto)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("KFPCL_SESSION_ID"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.user.role", is("SELLER")))
                .andExpect(jsonPath("$.data.user.isVerified", is(false))); // OTP verification does NOT approve KYC!
    }
}
