package com.kfpcl.controller;

import com.kfpcl.dto.request.QuotationSubmitRequest;
import com.kfpcl.dto.response.ApiResponse;
import com.kfpcl.dto.response.PageResponse;
import com.kfpcl.dto.response.QuotationResponse;
import com.kfpcl.entity.enums.QuotationStatus;
import com.kfpcl.service.QuotationService;
import com.kfpcl.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/seller/rfq")
@RequiredArgsConstructor
public class SellerQuotationController {

    private final QuotationService quotationService;

    /**
     * Protected API: Submit or update a commercial quotation for an open RFQ.
     * POST /api/v1/seller/rfq/{rfqId}/quote
     */
    @PostMapping("/{rfqId}/quote")
    public ResponseEntity<ApiResponse<QuotationResponse>> submitQuote(
            @PathVariable Long rfqId,
            @Valid @RequestBody QuotationSubmitRequest request,
            Principal principal) {
        String email = principal != null ? principal.getName() : SecurityUtil.getCurrentUserEmail();
        QuotationResponse response = quotationService.submitQuote(email, rfqId, request);
        return new ResponseEntity<>(
                ApiResponse.success("Quotation submitted successfully", response),
                HttpStatus.CREATED
        );
    }

    /**
     * Protected API: List commercial quotations submitted by the authenticated seller.
     * GET /api/v1/seller/rfq/quotes
     */
    @GetMapping("/quotes")
    public ResponseEntity<ApiResponse<PageResponse<QuotationResponse>>> getSellerQuotations(
            @RequestParam(value = "status", required = false) QuotationStatus status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Principal principal) {
        String email = principal != null ? principal.getName() : SecurityUtil.getCurrentUserEmail();
        PageResponse<QuotationResponse> response = quotationService.getSellerQuotations(email, status, page, size);
        return ResponseEntity.ok(ApiResponse.success("Seller quotations retrieved successfully", response));
    }
}
