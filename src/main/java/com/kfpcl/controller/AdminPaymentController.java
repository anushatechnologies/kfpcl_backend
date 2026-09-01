package com.kfpcl.controller;

import com.kfpcl.dto.ApiResponse;
import com.kfpcl.dto.PageResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/payments")
public class AdminPaymentController {

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDto<Map<String, Object>>>> listPayments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // Stub implementation for frontend integration
        PageResponseDto<Map<String, Object>> response = PageResponseDto.<Map<String, Object>>builder()
                .content(Collections.emptyList())
                .page(page)
                .size(size)
                .totalElements(0)
                .totalPages(0)
                .isFirst(true)
                .isLast(true)
                .build();
        return ResponseEntity.ok(ApiResponse.success(response, "Payments retrieved successfully"));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPayment(@PathVariable String paymentId) {
        return ResponseEntity.ok(ApiResponse.success(Map.of("id", paymentId, "status", "PAID"), "Payment retrieved successfully"));
    }
}
