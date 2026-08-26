package com.kfpcl.kyc.controller;

import com.kfpcl.common.response.ApiResponse;
import com.kfpcl.common.security.RequireRole;
import com.kfpcl.common.security.UserContext;
import com.kfpcl.kyc.dto.KycResubmitRequestDto;
import com.kfpcl.kyc.dto.KycStatusResponseDto;
import com.kfpcl.kyc.service.KycService;
import com.kfpcl.user.entity.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Seller KYC", description = "Endpoints for authenticated suppliers to check and resubmit KYC documents")
@RestController
@RequestMapping("/api/v1/seller")
public class SellerKycController {

    private final KycService kycService;

    public SellerKycController(KycService kycService) {
        this.kycService = kycService;
    }

    @Operation(summary = "Get Seller KYC Status", description = "Checks the KYC verification and approval status for the authenticated supplier")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "KYC status fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Missing or expired session"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Role mismatch (supplier only)")
    })
    @RequireRole(Role.SELLER)
    @GetMapping("/kyc-status")
    public ResponseEntity<ApiResponse<KycStatusResponseDto>> getKycStatus() {
        String sellerId = UserContext.getRequiredUserId();
        KycStatusResponseDto responseDto = kycService.getKycStatus(sellerId);
        return ResponseEntity.ok(ApiResponse.success("KYC status fetched successfully", responseDto));
    }

    @Operation(summary = "Resubmit Seller KYC", description = "Allows an authenticated supplier to re-submit corrected KYC documents and information")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "KYC documents resubmitted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Missing or expired session"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Role mismatch (supplier only)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "KYC is already approved")
    })
    @RequireRole(Role.SELLER)
    @PostMapping("/resubmit-kyc")
    public ResponseEntity<ApiResponse<KycStatusResponseDto>> resubmitKyc(@Valid @RequestBody KycResubmitRequestDto request) {
        String sellerId = UserContext.getRequiredUserId();
        KycStatusResponseDto responseDto = kycService.resubmitKyc(sellerId, request);
        return ResponseEntity.ok(ApiResponse.success("KYC documents resubmitted successfully", responseDto));
    }
}
