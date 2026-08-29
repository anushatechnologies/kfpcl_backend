package com.kfpcl.controller;

import com.kfpcl.dto.ApiResponse;
import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.dto.request.InquiryRequest;
import com.kfpcl.dto.response.InquiryResponse;
import com.kfpcl.service.InquiryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/buyer/inquiries")
@RequiredArgsConstructor
public class BuyerInquiryController {

    private final InquiryService inquiryService;

    @PostMapping
    public ResponseEntity<ApiResponse<InquiryResponse>> sendInquiry(
            @RequestBody InquiryRequest requestDto,
            HttpServletRequest request) {
        
        String buyerId = (String) request.getAttribute("authenticatedUser");
        InquiryResponse response = inquiryService.createInquiry(buyerId, requestDto);
        return ResponseEntity.ok(ApiResponse.success(response, "Inquiry sent successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDto<InquiryResponse>>> listInquiries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        
        String buyerId = (String) request.getAttribute("authenticatedUser");
        PageResponseDto<InquiryResponse> responses = inquiryService.getBuyerInquiries(buyerId, page, size);
        return ResponseEntity.ok(ApiResponse.success(responses, "Inquiries retrieved successfully"));
    }
}
