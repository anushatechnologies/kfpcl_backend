package com.kfpcl.controller;

import com.kfpcl.dto.ApiResponse;
import com.kfpcl.dto.CreateRfqRequest;
import com.kfpcl.dto.RfqResponse;
import com.kfpcl.service.RfqService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/buyer/rfqs")
@RequiredArgsConstructor
public class BuyerRfqController {

    private final RfqService rfqService;

    @PostMapping
    public ResponseEntity<ApiResponse<RfqResponse>> createRfq(
            @Valid @RequestBody CreateRfqRequest request
    ) {
        RfqResponse response = rfqService.createRfq(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RfqResponse>>> getBuyerRfqs() {
        List<RfqResponse> rfqs = rfqService.getBuyerRfqs();
        return ResponseEntity.ok(ApiResponse.success(rfqs));
    }

    @GetMapping("/{rfqId}")
    public ResponseEntity<ApiResponse<RfqResponse>> getBuyerRfqById(
            @PathVariable("rfqId") String rfqId
    ) {
        RfqResponse rfq = rfqService.getBuyerRfqById(rfqId);
        return ResponseEntity.ok(ApiResponse.success(rfq));
    }

    @PatchMapping("/{rfqId}/cancel")
    public ResponseEntity<ApiResponse<RfqResponse>> cancelRfq(
            @PathVariable("rfqId") String rfqId
    ) {
        RfqResponse rfq = rfqService.cancelRfq(rfqId);
        return ResponseEntity.ok(ApiResponse.success(rfq));
    }
}
