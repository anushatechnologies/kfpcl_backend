package com.payment.controller;

import com.payment.dto.payout.SellerPayoutResponse;
import com.payment.entity.enums.PayoutStatus;
import com.payment.service.SellerPayoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/seller/payments")
@RequiredArgsConstructor
@Tag(name = "Seller Payouts", description = "Endpoints for Seller Payout Statements, Platform Commission, and TDS Deductions")
public class SellerPayoutController {

    private final SellerPayoutService sellerPayoutService;

    @Operation(summary = "12. Seller Payout Statements",
            description = "Retrieves paginated payout statements for the authenticated seller with platform commission (2%), TDS deduction (1%), net payout, and bank transfer references.")
    @GetMapping("/payouts")
    public ResponseEntity<SellerPayoutResponse> getSellerPayouts(
            @Parameter(description = "Filter by payout status (PENDING, PROCESSING, COMPLETED, FAILED)")
            @RequestParam(required = false) PayoutStatus status,

            @Parameter(description = "Start datetime filter (ISO-8601, e.g., 2026-08-01T00:00:00)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,

            @Parameter(description = "End datetime filter (ISO-8601, e.g., 2026-08-31T23:59:59)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,

            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "initiatedAt") String sortBy,

            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "desc") String sortDir) {

        SellerPayoutResponse response = sellerPayoutService.getSellerPayouts(
                status, startDate, endDate, page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }
}
