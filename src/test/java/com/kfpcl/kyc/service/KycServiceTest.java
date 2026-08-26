package com.kfpcl.kyc.service;

import com.kfpcl.admin.service.AdminKycServiceImpl;
import com.kfpcl.common.exception.KycAlreadyApprovedException;
import com.kfpcl.common.exception.UserNotFoundException;
import com.kfpcl.kyc.dto.KycApprovalResponseDto;
import com.kfpcl.kyc.dto.KycRejectionRequestDto;
import com.kfpcl.kyc.dto.KycResubmitRequestDto;
import com.kfpcl.kyc.dto.KycStatusResponseDto;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KycServiceTest {

    @Mock
    private UserRepository userRepository;

    private KycServiceImpl kycService;
    private AdminKycServiceImpl adminKycService;

    @BeforeEach
    void setUp() {
        kycService = new KycServiceImpl(userRepository);
        adminKycService = new AdminKycServiceImpl(userRepository);
    }

    @Test
    @DisplayName("Should retrieve seller KYC status accurately")
    void testGetKycStatus() {
        User seller = User.builder()
                .id("usr_seller1")
                .role(Role.SELLER)
                .isVerified(false)
                .kycStatus(KycStatus.PENDING)
                .build();

        when(userRepository.findByIdAndIsActiveTrue("usr_seller1")).thenReturn(Optional.of(seller));

        KycStatusResponseDto response = kycService.getKycStatus("usr_seller1");

        assertNotNull(response);
        assertFalse(response.isVerified());
        assertEquals(KycStatus.PENDING, response.getKycStatus());
        assertNull(response.getRejectionReason());
    }

    @Test
    @DisplayName("Should allow resubmission of rejected KYC documents")
    void testResubmitKyc_Success() {
        User seller = User.builder()
                .id("usr_seller1")
                .role(Role.SELLER)
                .isVerified(false)
                .kycStatus(KycStatus.REJECTED)
                .rejectionReason("Blurry PAN document")
                .build();

        when(userRepository.findByIdAndIsActiveTrue("usr_seller1")).thenReturn(Optional.of(seller));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        KycResubmitRequestDto resubmitDto = KycResubmitRequestDto.builder()
                .gstNumber("27AABCA1234F1Z1")
                .panNumber("AABCA1234F")
                .kycDocUrl("https://storage/new_gst.pdf")
                .panDocUrl("https://storage/new_pan.jpg")
                .build();

        KycStatusResponseDto response = kycService.resubmitKyc("usr_seller1", resubmitDto);

        assertNotNull(response);
        assertEquals(KycStatus.SUBMITTED, response.getKycStatus());
        assertFalse(response.isVerified());
        assertNull(response.getRejectionReason());
    }

    @Test
    @DisplayName("Should prevent resubmission if KYC is already approved")
    void testResubmitKyc_AlreadyApproved() {
        User approvedSeller = User.builder()
                .id("usr_seller1")
                .role(Role.SELLER)
                .isVerified(true)
                .kycStatus(KycStatus.APPROVED)
                .build();

        when(userRepository.findByIdAndIsActiveTrue("usr_seller1")).thenReturn(Optional.of(approvedSeller));

        KycResubmitRequestDto resubmitDto = KycResubmitRequestDto.builder()
                .gstNumber("27AABCA1234F1Z1")
                .panNumber("AABCA1234F")
                .kycDocUrl("https://storage/new_gst.pdf")
                .panDocUrl("https://storage/new_pan.jpg")
                .build();

        assertThrows(KycAlreadyApprovedException.class, () ->
                kycService.resubmitKyc("usr_seller1", resubmitDto)
        );
    }

    @Test
    @DisplayName("Admin approve KYC should set isVerified = true and kycStatus = APPROVED")
    void testAdminApproveKyc() {
        User seller = User.builder()
                .id("usr_seller1")
                .role(Role.SELLER)
                .isVerified(false)
                .kycStatus(KycStatus.SUBMITTED)
                .build();

        when(userRepository.findByIdAndIsActiveTrue("usr_seller1")).thenReturn(Optional.of(seller));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        KycApprovalResponseDto response = adminKycService.approveKyc("usr_seller1", "admin_007");

        assertNotNull(response);
        assertTrue(response.isVerified());
        assertEquals(KycStatus.APPROVED, response.getKycStatus());
        assertEquals("admin_007", response.getApprovedByAdminId());
        assertNotNull(response.getApprovedAt());
    }

    @Test
    @DisplayName("Admin reject KYC should set isVerified = false, kycStatus = REJECTED, and record reason")
    void testAdminRejectKyc() {
        User seller = User.builder()
                .id("usr_seller1")
                .role(Role.SELLER)
                .isVerified(false)
                .kycStatus(KycStatus.SUBMITTED)
                .build();

        when(userRepository.findByIdAndIsActiveTrue("usr_seller1")).thenReturn(Optional.of(seller));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        KycRejectionRequestDto rejectionDto = KycRejectionRequestDto.builder()
                .rejectionReason("GST certificate is invalid or unreadable")
                .build();

        KycStatusResponseDto response = adminKycService.rejectKyc("usr_seller1", rejectionDto, "admin_007");

        assertNotNull(response);
        assertFalse(response.isVerified());
        assertEquals(KycStatus.REJECTED, response.getKycStatus());
        assertEquals("GST certificate is invalid or unreadable", response.getRejectionReason());
    }
}
