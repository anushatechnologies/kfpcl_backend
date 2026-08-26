package com.kfpcl.controller;

import com.kfpcl.dto.request.RfqCreateRequest;
import com.kfpcl.dto.response.ApiResponse;
import com.kfpcl.dto.response.PageResponse;
import com.kfpcl.dto.response.QuoteAcceptanceResponse;
import com.kfpcl.dto.response.RfqComparisonResponse;
import com.kfpcl.dto.response.RfqResponse;
import com.kfpcl.entity.enums.RFQStatus;
import com.kfpcl.service.BuyerRfqComparisonService;
import com.kfpcl.service.QuoteAcceptanceService;
import com.kfpcl.service.RfqService;
import com.kfpcl.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/buyer/rfq")
@RequiredArgsConstructor
public class BuyerRfqController {

    private final RfqService rfqService;
    private final BuyerRfqComparisonService buyerRfqComparisonService;
    private final QuoteAcceptanceService quoteAcceptanceService;

    /**
     * Protected API: Broadcast a custom bulk buying requirement (RFQ).
     * POST /api/v1/buyer/rfq
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RfqResponse>> createRfq(
            @Valid @RequestBody RfqCreateRequest request,
            Principal principal) {
        String email = principal != null ? principal.getName() : SecurityUtil.getCurrentUserEmail();
        RfqResponse response = rfqService.createRfq(email, request);
        return new ResponseEntity<>(
                ApiResponse.success("RFQ created and broadcasted successfully", response),
                HttpStatus.CREATED
        );
    }

    /**
     * Protected API: List RFQs created by the authenticated buyer.
     * GET /api/v1/buyer/rfq
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<RfqResponse>>> getBuyerRfqs(
            @RequestParam(value = "status", required = false) RFQStatus status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Principal principal) {
        String email = principal != null ? principal.getName() : SecurityUtil.getCurrentUserEmail();
        PageResponse<RfqResponse> response = rfqService.getBuyerRfqs(email, status, page, size);
        return ResponseEntity.ok(ApiResponse.success("Buyer RFQs retrieved successfully", response));
    }

    /**
     * Protected API: Get single RFQ details for the authenticated buyer.
     * GET /api/v1/buyer/rfq/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RfqResponse>> getBuyerRfqById(
            @PathVariable Long id,
            Principal principal) {
        String email = principal != null ? principal.getName() : SecurityUtil.getCurrentUserEmail();
        RfqResponse response = rfqService.getBuyerRfqById(email, id);
        return ResponseEntity.ok(ApiResponse.success("RFQ details retrieved successfully", response));
    }

    /**
     * Protected API: Compare competing quotations submitted for a specific buyer RFQ.
     * GET /api/v1/buyer/rfq/{rfqId}/quotes
     */
    @GetMapping("/{rfqId}/quotes")
    public ResponseEntity<ApiResponse<RfqComparisonResponse>> getRfqQuotationsComparison(
            @PathVariable Long rfqId,
            @RequestParam(value = "sortBy", defaultValue = "totalAmount") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "ASC") String sortDir,
            Principal principal) {
        String email = principal != null ? principal.getName() : SecurityUtil.getCurrentUserEmail();
        RfqComparisonResponse response = buyerRfqComparisonService.getRfqQuotationsComparison(
                email, rfqId, sortBy, sortDir
        );
        return ResponseEntity.ok(ApiResponse.success("RFQ quotations comparison retrieved successfully", response));
    }

    /**
     * Protected API: Transactional Quote Acceptance.
     * Accepts quotation, closes competing quotations, marks RFQ as AWARDED, and emits order event.
     * POST /api/v1/buyer/rfq/{rfqId}/quotes/{quoteId}/accept
     */
    @PostMapping("/{rfqId}/quotes/{quoteId}/accept")
    public ResponseEntity<ApiResponse<QuoteAcceptanceResponse>> acceptQuote(
            @PathVariable Long rfqId,
            @PathVariable Long quoteId,
            Principal principal) {
        String email = principal != null ? principal.getName() : SecurityUtil.getCurrentUserEmail();
        QuoteAcceptanceResponse response = quoteAcceptanceService.acceptQuote(email, rfqId, quoteId);
        return ResponseEntity.ok(ApiResponse.success(response.getMessage(), response));
    }

    /**
     * Protected API: Cancel an open RFQ.
     * PATCH /api/v1/buyer/rfq/{id}/cancel
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<RfqResponse>> cancelRfq(
            @PathVariable Long id,
            Principal principal) {
        String email = principal != null ? principal.getName() : SecurityUtil.getCurrentUserEmail();
        RfqResponse response = rfqService.cancelRfq(email, id);
        return ResponseEntity.ok(ApiResponse.success("RFQ cancelled successfully", response));
    }
}
