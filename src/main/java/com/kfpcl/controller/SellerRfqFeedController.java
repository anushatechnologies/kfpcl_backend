package com.kfpcl.controller;

import com.kfpcl.dto.ApiResponse;
import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.dto.QuotationResponseDto;
import com.kfpcl.dto.RfqResponseDto;
import com.kfpcl.dto.request.QuotationSubmitRequest;
import com.kfpcl.service.SellerRfqFeedService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/seller/rfq")
@RequiredArgsConstructor
public class SellerRfqFeedController {

    private final SellerRfqFeedService rfqFeedService;

    @GetMapping("/feed")
    public ResponseEntity<ApiResponse<PageResponseDto<RfqResponseDto>>> listOpenRfqs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        PageResponseDto<RfqResponseDto> responses = rfqFeedService.getOpenRfqs(page, size);
        return ResponseEntity.ok(ApiResponse.success(responses, "RFQ feed retrieved successfully"));
    }

    @PostMapping("/{rfqId}/quote")
    public ResponseEntity<ApiResponse<QuotationResponseDto>> submitQuote(
            @PathVariable String rfqId,
            @RequestBody QuotationSubmitRequest requestDto,
            HttpServletRequest request) {
        
        String sellerId = (String) request.getAttribute("authenticatedUser");
        QuotationResponseDto response = rfqFeedService.submitQuote(sellerId, rfqId, requestDto);
        return ResponseEntity.ok(ApiResponse.success(response, "Quote submitted successfully"));
    }
}
