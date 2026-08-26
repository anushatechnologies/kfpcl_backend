package com.kfpcl.controller;

import com.kfpcl.dto.request.InquiryCreateRequest;
import com.kfpcl.dto.response.ApiResponse;
import com.kfpcl.dto.response.InquiryResponse;
import com.kfpcl.dto.response.PageResponse;
import com.kfpcl.entity.enums.InquiryStatus;
import com.kfpcl.service.InquiryService;
import com.kfpcl.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/buyer/inquiries")
@RequiredArgsConstructor
public class BuyerInquiryController {

    private final InquiryService inquiryService;

    /**
     * Protected API: Send direct product inquiry to a seller.
     * POST /api/v1/buyer/inquiries
     */
    @PostMapping
    public ResponseEntity<ApiResponse<InquiryResponse>> createInquiry(
            @Valid @RequestBody InquiryCreateRequest request,
            Principal principal) {
        String email = principal != null ? principal.getName() : SecurityUtil.getCurrentUserEmail();
        InquiryResponse response = inquiryService.createInquiry(email, request);
        return new ResponseEntity<>(
                ApiResponse.success("Inquiry sent successfully to seller", response),
                HttpStatus.CREATED
        );
    }

    /**
     * Protected API: List buyer inquiries with pagination and status filter.
     * GET /api/v1/buyer/inquiries
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<InquiryResponse>>> getBuyerInquiries(
            @RequestParam(value = "status", required = false) InquiryStatus status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Principal principal) {
        String email = principal != null ? principal.getName() : SecurityUtil.getCurrentUserEmail();
        PageResponse<InquiryResponse> response = inquiryService.getBuyerInquiries(email, status, page, size);
        return ResponseEntity.ok(ApiResponse.success("Buyer inquiries retrieved successfully", response));
    }
}
