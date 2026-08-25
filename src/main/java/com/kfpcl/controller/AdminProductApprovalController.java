package com.kfpcl.controller;

import com.kfpcl.dto.ApiResponse;
import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.dto.ProductApprovalActionDto;
import com.kfpcl.dto.ProductApprovalResponseDto;
import com.kfpcl.service.AdminProductApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/catalog/product-approvals")
@RequiredArgsConstructor
public class AdminProductApprovalController {

    private final AdminProductApprovalService adminProductApprovalService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDto<ProductApprovalResponseDto>>> listProductApprovals(
            @RequestParam(defaultValue = "PENDING") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        PageResponseDto<ProductApprovalResponseDto> approvals = adminProductApprovalService.getProductApprovals(status, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(approvals, "Product approvals retrieved successfully"));
    }

    @GetMapping("/{approvalId}")
    public ResponseEntity<ApiResponse<ProductApprovalResponseDto>> getApprovalDetails(
            @PathVariable String approvalId) {

        ProductApprovalResponseDto approval = adminProductApprovalService.getApprovalById(approvalId);
        return ResponseEntity.ok(ApiResponse.success(approval, "Product approval details retrieved successfully"));
    }

    @PostMapping("/{approvalId}/approve")
    public ResponseEntity<ApiResponse<ProductApprovalResponseDto>> approveProduct(
            @PathVariable String approvalId,
            @RequestBody(required = false) ProductApprovalActionDto dto) {

        ProductApprovalResponseDto approved = adminProductApprovalService.approveProduct(approvalId, dto);
        return ResponseEntity.ok(ApiResponse.success(approved, "Product approved successfully"));
    }

    @PostMapping("/{approvalId}/reject")
    public ResponseEntity<ApiResponse<ProductApprovalResponseDto>> rejectProduct(
            @PathVariable String approvalId,
            @RequestBody(required = false) ProductApprovalActionDto dto) {

        ProductApprovalResponseDto rejected = adminProductApprovalService.rejectProduct(approvalId, dto);
        return ResponseEntity.ok(ApiResponse.success(rejected, "Product rejected successfully"));
    }
}
