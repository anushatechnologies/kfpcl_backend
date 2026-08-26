package com.kfpcl.admin.controller;

import com.kfpcl.admin.service.AdminKycService;
import com.kfpcl.common.response.ApiResponse;
import com.kfpcl.common.security.RequireRole;
import com.kfpcl.common.security.UserContext;
import com.kfpcl.kyc.dto.KycApprovalResponseDto;
import com.kfpcl.kyc.dto.KycRejectionRequestDto;
import com.kfpcl.kyc.dto.KycStatusResponseDto;
import com.kfpcl.user.entity.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin KYC Management", description = "Endpoints for platform administrators to review, approve, and reject supplier KYC")
@RestController
@RequestMapping("/api/v1/admin/suppliers")
public class AdminKycController {

    private final AdminKycService adminKycService;

    public AdminKycController(AdminKycService adminKycService) {
        this.adminKycService = adminKycService;
    }

    @Operation(summary = "Approve Supplier KYC", description = "Approves a supplier's KYC documents and sets verified status to true")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Supplier KYC approved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Missing or expired admin session"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Role mismatch (admin only)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Supplier not found")
    })
    @RequireRole(Role.ADMIN)
    @PutMapping("/{id}/approve-kyc")
    public ResponseEntity<ApiResponse<KycApprovalResponseDto>> approveKyc(@PathVariable("id") String id) {
        String adminId = UserContext.getRequiredUserId();
        KycApprovalResponseDto responseDto = adminKycService.approveKyc(id, adminId);
        return ResponseEntity.ok(ApiResponse.success("Supplier KYC approved successfully", responseDto));
    }

    @Operation(summary = "Reject Supplier KYC", description = "Rejects a supplier's KYC with a reason and marks verification as false")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Supplier KYC rejected successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error (missing reason)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Missing or expired admin session"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Role mismatch (admin only)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Supplier not found")
    })
    @RequireRole(Role.ADMIN)
    @PutMapping("/{id}/reject-kyc")
    public ResponseEntity<ApiResponse<KycStatusResponseDto>> rejectKyc(@PathVariable("id") String id,
                                                                       @Valid @RequestBody KycRejectionRequestDto request) {
        String adminId = UserContext.getRequiredUserId();
        KycStatusResponseDto responseDto = adminKycService.rejectKyc(id, request, adminId);
        return ResponseEntity.ok(ApiResponse.success("Supplier KYC rejected", responseDto));
    }
}
