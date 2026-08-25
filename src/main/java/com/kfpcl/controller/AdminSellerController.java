package com.kfpcl.controller;

import com.kfpcl.dto.*;
import com.kfpcl.service.AdminSellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/sellers")
@RequiredArgsConstructor
public class AdminSellerController {

    private final AdminSellerService adminSellerService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDto<AdminUserResponseDto>>> listSellers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        PageResponseDto<AdminUserResponseDto> sellers = adminSellerService.getSellers(search, status, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(sellers, "Sellers retrieved successfully"));
    }

    @GetMapping("/applications")
    public ResponseEntity<ApiResponse<PageResponseDto<SellerApplicationResponseDto>>> listApplications(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        PageResponseDto<SellerApplicationResponseDto> applications = adminSellerService.getApplications(status, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(applications, "Seller applications retrieved successfully"));
    }

    @GetMapping("/applications/{applicationId}")
    public ResponseEntity<ApiResponse<SellerApplicationResponseDto>> getApplication(
            @PathVariable String applicationId) {

        SellerApplicationResponseDto application = adminSellerService.getApplicationById(applicationId);
        return ResponseEntity.ok(ApiResponse.success(application, "Seller application details retrieved successfully"));
    }

    @PostMapping("/applications/{applicationId}/approve")
    public ResponseEntity<ApiResponse<SellerApplicationResponseDto>> approveApplication(
            @PathVariable String applicationId,
            @RequestBody(required = false) SellerActionDto dto) {

        SellerApplicationResponseDto approved = adminSellerService.approveApplication(applicationId, dto);
        return ResponseEntity.ok(ApiResponse.success(approved, "Seller application approved successfully"));
    }

    @PostMapping("/applications/{applicationId}/reject")
    public ResponseEntity<ApiResponse<SellerApplicationResponseDto>> rejectApplication(
            @PathVariable String applicationId,
            @RequestBody(required = false) SellerActionDto dto) {

        SellerApplicationResponseDto rejected = adminSellerService.rejectApplication(applicationId, dto);
        return ResponseEntity.ok(ApiResponse.success(rejected, "Seller application rejected successfully"));
    }

    @GetMapping("/stores")
    public ResponseEntity<ApiResponse<PageResponseDto<SellerStoreResponseDto>>> listSellerStores(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        PageResponseDto<SellerStoreResponseDto> stores = adminSellerService.getSellerStores(search, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(stores, "Seller stores retrieved successfully"));
    }

    @GetMapping("/{sellerId}/verification")
    public ResponseEntity<ApiResponse<SellerApplicationResponseDto>> getSellerVerification(
            @PathVariable String sellerId) {

        SellerApplicationResponseDto verification = adminSellerService.getSellerVerification(sellerId);
        return ResponseEntity.ok(ApiResponse.success(verification, "Seller verification retrieved successfully"));
    }
}
