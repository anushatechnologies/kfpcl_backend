package com.payment.controller;

import com.payment.dto.history.PaymentHistoryResponse;
import com.payment.entity.enums.PaymentMethod;
import com.payment.entity.enums.PaymentStatus;
import com.payment.service.BuyerPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/buyer/payments")
@RequiredArgsConstructor
@Tag(name = "Buyer Payments", description = "Endpoints for Buyer Payment History, Invoices, and Statements")
public class BuyerPaymentController {

    private final BuyerPaymentService buyerPaymentService;

    @Operation(summary = "9. Buyer Payment History", description = "Retrieves paginated, filtered payment history for the authenticated buyer.")
    @GetMapping("/history")
    public ResponseEntity<PaymentHistoryResponse> getBuyerPaymentHistory(
            @Parameter(description = "Filter by payment status")
            @RequestParam(required = false) PaymentStatus status,

            @Parameter(description = "Filter by payment method (CARD, UPI, BANK, LC, NETBANKING)")
            @RequestParam(required = false) PaymentMethod paymentMethod,

            @Parameter(description = "Start datetime filter (ISO-8601, e.g., 2026-08-01T00:00:00)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,

            @Parameter(description = "End datetime filter (ISO-8601, e.g., 2026-08-31T23:59:59)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,

            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "desc") String sortDir) {

        PaymentHistoryResponse response = buyerPaymentService.getBuyerPaymentHistory(
                status, paymentMethod, startDate, endDate, page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }
}
