package com.kfpcl.controller;

import com.kfpcl.dto.ApiResponse;
import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.dto.request.InquiryReplyRequest;
import com.kfpcl.dto.response.InquiryResponse;
import com.kfpcl.service.InquiryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/seller/inquiries")
@RequiredArgsConstructor
public class SellerInquiryController {

    private final InquiryService inquiryService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDto<InquiryResponse>>> listInquiries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        
        String sellerId = (String) request.getAttribute("authenticatedUser");
        PageResponseDto<InquiryResponse> responses = inquiryService.getSellerInquiries(sellerId, page, size);
        return ResponseEntity.ok(ApiResponse.success(responses, "Leads retrieved successfully"));
    }

    @PostMapping("/{id}/reply")
    public ResponseEntity<ApiResponse<InquiryResponse>> replyToInquiry(
            @PathVariable String id,
            @RequestBody InquiryReplyRequest requestDto,
            HttpServletRequest request) {
        
        String sellerId = (String) request.getAttribute("authenticatedUser");
        InquiryResponse response = inquiryService.replyToInquiry(sellerId, id, requestDto.getReplyMessage());
        return ResponseEntity.ok(ApiResponse.success(response, "Reply sent successfully"));
    }
}
