package com.kfpcl.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kfpcl.kyc.dto.KycRejectionRequestDto;
import com.kfpcl.session.entity.UserSession;
import com.kfpcl.session.repository.UserSessionRepository;
import com.kfpcl.user.entity.KycStatus;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class AdminKycIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSessionRepository sessionRepository;

    private User adminUser;
    private User sellerUser;
    private Cookie adminCookie;

    @BeforeEach
    void setUp() {
        sessionRepository.deleteAll();
        userRepository.deleteAll();

        // Create Admin User & Session
        adminUser = User.builder()
                .id("usr_admin001")
                .phone("9999999999")
                .role(Role.ADMIN)
                .isVerified(true)
                .isActive(true)
                .build();
        userRepository.save(adminUser);

        UserSession adminSession = UserSession.builder()
                .sessionId("sess_admin123")
                .userId(adminUser.getId())
                .role(Role.ADMIN)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(8))
                .lastAccessedAt(LocalDateTime.now())
                .isActive(true)
                .build();
        sessionRepository.save(adminSession);
        adminCookie = new Cookie("KFPCL_SESSION_ID", "sess_admin123");

        // Create Seller User with Submitted KYC
        sellerUser = User.builder()
                .id("usr_seller001")
                .phone("9822011223")
                .ownerName("Vikram Singhania")
                .companyName("Apex Machinery")
                .gstNumber("27AABCA1234F1Z1")
                .panNumber("AABCA1234F")
                .role(Role.SELLER)
                .isVerified(false)
                .kycStatus(KycStatus.SUBMITTED)
                .isActive(true)
                .build();
        userRepository.save(sellerUser);
    }

    @Test
    @DisplayName("Admin successfully approves supplier KYC")
    void testAdminApproveKyc_Success() throws Exception {
        mockMvc.perform(put("/api/v1/admin/suppliers/" + sellerUser.getId() + "/approve-kyc")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.isVerified", is(true)))
                .andExpect(jsonPath("$.data.kycStatus", is("APPROVED")))
                .andExpect(jsonPath("$.data.approvedByAdminId", is(adminUser.getId())));
    }

    @Test
    @DisplayName("Admin successfully rejects supplier KYC with reason")
    void testAdminRejectKyc_Success() throws Exception {
        KycRejectionRequestDto rejectionDto = KycRejectionRequestDto.builder()
                .rejectionReason("GST certificate is invalid or unreadable")
                .build();

        mockMvc.perform(put("/api/v1/admin/suppliers/" + sellerUser.getId() + "/reject-kyc")
                        .cookie(adminCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectionDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.isVerified", is(false)))
                .andExpect(jsonPath("$.data.kycStatus", is("REJECTED")))
                .andExpect(jsonPath("$.data.rejectionReason", is("GST certificate is invalid or unreadable")));
    }
}
