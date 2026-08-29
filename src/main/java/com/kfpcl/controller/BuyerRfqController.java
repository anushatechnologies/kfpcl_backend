package com.kfpcl.controller;

import com.kfpcl.dto.ApiResponse;
import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.dto.RfqResponseDto;
import com.kfpcl.dto.request.RfqCreateRequest;
import com.kfpcl.dto.response.QuoteAcceptanceResponse;
import com.kfpcl.service.BuyerRfqService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/buyer/rfq")
@RequiredArgsConstructor
public class BuyerRfqController {

    private final BuyerRfqService buyerRfqService;

    @PostMapping
    public ResponseEntity<ApiResponse<RfqResponseDto>> createRfq(
            @RequestBody RfqCreateRequest requestDto,
            HttpServletRequest request) {
        
        String buyerId = (String) request.getAttribute("authenticatedUser");
        RfqResponseDto response = buyerRfqService.createRfq(buyerId, requestDto);
        return ResponseEntity.ok(ApiResponse.success(response, "RFQ created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDto<RfqResponseDto>>> listRfqs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        
        String buyerId = (String) request.getAttribute("authenticatedUser");
        PageResponseDto<RfqResponseDto> responses = buyerRfqService.getBuyerRfqs(buyerId, page, size);
        return ResponseEntity.ok(ApiResponse.success(responses, "RFQs retrieved successfully"));
    }

    @PostMapping("/{rfqId}/quotes/{quoteId}/accept")
    public ResponseEntity<ApiResponse<QuoteAcceptanceResponse>> acceptQuote(
            @PathVariable String rfqId,
            @PathVariable String quoteId,
            HttpServletRequest request) {
        
        String buyerId = (String) request.getAttribute("authenticatedUser");
        QuoteAcceptanceResponse response = buyerRfqService.acceptQuote(buyerId, rfqId, quoteId);
        return ResponseEntity.ok(ApiResponse.success(response, "Quote accepted successfully"));
    }
}
