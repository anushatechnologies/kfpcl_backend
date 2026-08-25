package com.kfpcl.controller;

import com.kfpcl.dto.ApiResponse;
import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.dto.QuotationResponseDto;
import com.kfpcl.service.AdminQuotationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/quotations")
@RequiredArgsConstructor
public class AdminQuotationController {

    private final AdminQuotationService adminQuotationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDto<QuotationResponseDto>>> listQuotations(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        PageResponseDto<QuotationResponseDto> quotations = adminQuotationService.getQuotations(status, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(quotations, "Quotations retrieved successfully"));
    }

    @GetMapping("/{quotationId}")
    public ResponseEntity<ApiResponse<QuotationResponseDto>> getQuotation(@PathVariable String quotationId) {
        QuotationResponseDto quotation = adminQuotationService.getQuotationById(quotationId);
        return ResponseEntity.ok(ApiResponse.success(quotation, "Quotation details retrieved successfully"));
    }
}
