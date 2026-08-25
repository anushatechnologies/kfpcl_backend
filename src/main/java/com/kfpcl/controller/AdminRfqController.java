package com.kfpcl.controller;

import com.kfpcl.dto.ApiResponse;
import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.dto.QuotationResponseDto;
import com.kfpcl.dto.RfqResponseDto;
import com.kfpcl.service.AdminRfqService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/rfqs")
@RequiredArgsConstructor
public class AdminRfqController {

    private final AdminRfqService adminRfqService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDto<RfqResponseDto>>> listRfqs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        PageResponseDto<RfqResponseDto> rfqs = adminRfqService.getRfqs(search, status, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(rfqs, "RFQs retrieved successfully"));
    }

    @GetMapping("/{rfqId}")
    public ResponseEntity<ApiResponse<RfqResponseDto>> getRfq(@PathVariable String rfqId) {
        RfqResponseDto rfq = adminRfqService.getRfqById(rfqId);
        return ResponseEntity.ok(ApiResponse.success(rfq, "RFQ details retrieved successfully"));
    }

    @GetMapping("/{rfqId}/quotations")
    public ResponseEntity<ApiResponse<List<QuotationResponseDto>>> getQuotationsForRfq(@PathVariable String rfqId) {
        List<QuotationResponseDto> quotations = adminRfqService.getQuotationsForRfq(rfqId);
        return ResponseEntity.ok(ApiResponse.success(quotations, "Quotations for RFQ retrieved successfully"));
    }
}
