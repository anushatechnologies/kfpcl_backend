package com.kfpcl.controller;

import com.kfpcl.dto.request.InquiryReplyRequest;
import com.kfpcl.dto.response.ApiResponse;
import com.kfpcl.dto.response.InquiryResponse;
import com.kfpcl.dto.response.PageResponse;
import com.kfpcl.entity.enums.InquiryStatus;
import com.kfpcl.service.InquiryService;
import com.kfpcl.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/seller/inquiries")
@RequiredArgsConstructor
public class SellerInquiryController {

    private final InquiryService inquiryService;

    /**
     * Protected API: List incoming buyer inquiries / leads for the seller.
     * GET /api/v1/seller/inquiries
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<InquiryResponse>>> getSellerInquiries(
            @RequestParam(value = "status", required = false) InquiryStatus status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Principal principal) {
        String email = principal != null ? principal.getName() : SecurityUtil.getCurrentUserEmail();
        PageResponse<InquiryResponse> response = inquiryService.getSellerInquiries(email, status, page, size);
        return ResponseEntity.ok(ApiResponse.success("Seller inquiries retrieved successfully", response));
    }

    /**
     * Protected API: Reply to an incoming buyer inquiry.
     * POST /api/v1/seller/inquiries/{id}/reply
     */
    @PostMapping("/{id}/reply")
    public ResponseEntity<ApiResponse<InquiryResponse>> replyToInquiry(
            @PathVariable Long id,
            @Valid @RequestBody InquiryReplyRequest request,
            Principal principal) {
        String email = principal != null ? principal.getName() : SecurityUtil.getCurrentUserEmail();
        InquiryResponse response = inquiryService.replyToInquiry(email, id, request);
        return ResponseEntity.ok(ApiResponse.success("Reply submitted successfully", response));
    }
}
