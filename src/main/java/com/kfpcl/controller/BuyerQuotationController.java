package com.kfpcl.controller;

import com.kfpcl.dto.ApiResponse;
import com.kfpcl.dto.BuyerOrderResponse;
import com.kfpcl.dto.QuotationCompareResponse;
import com.kfpcl.dto.QuotationResponse;
import com.kfpcl.service.QuotationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/buyer/rfqs/{rfqId}/quotations")
@RequiredArgsConstructor
public class BuyerQuotationController {

    private final QuotationService quotationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<QuotationResponse>>> getQuotations(
            @PathVariable("rfqId") String rfqId
    ) {
        List<QuotationResponse> quotations = quotationService.getQuotationsForRfq(rfqId);
        return ResponseEntity.ok(ApiResponse.success(quotations));
    }

    @GetMapping("/compare")
    public ResponseEntity<ApiResponse<QuotationCompareResponse>> compareQuotations(
            @PathVariable("rfqId") String rfqId
    ) {
        QuotationCompareResponse compareResponse = quotationService.compareQuotations(rfqId);
        return ResponseEntity.ok(ApiResponse.success(compareResponse));
    }

    @PostMapping("/{quotationId}/accept")
    public ResponseEntity<ApiResponse<BuyerOrderResponse>> acceptQuotation(
            @PathVariable("rfqId") String rfqId,
            @PathVariable("quotationId") String quotationId
    ) {
        BuyerOrderResponse orderResponse = quotationService.acceptQuotation(rfqId, quotationId);
        return ResponseEntity.ok(ApiResponse.success(orderResponse));
    }

    @PostMapping("/{quotationId}/reject")
    public ResponseEntity<ApiResponse<QuotationResponse>> rejectQuotation(
            @PathVariable("rfqId") String rfqId,
            @PathVariable("quotationId") String quotationId
    ) {
        QuotationResponse quotationResponse = quotationService.rejectQuotation(rfqId, quotationId);
        return ResponseEntity.ok(ApiResponse.success(quotationResponse));
    }
}
