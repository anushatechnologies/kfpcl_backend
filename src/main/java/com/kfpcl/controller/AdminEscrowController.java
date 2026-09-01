package com.kfpcl.controller;

import com.kfpcl.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/escrow")
public class AdminEscrowController {

    @GetMapping("/settlements")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listSettlements() {
        return ResponseEntity.ok(ApiResponse.success(Collections.emptyList(), "Escrow settlements retrieved successfully"));
    }

    @PostMapping("/{escrowId}/release")
    public ResponseEntity<ApiResponse<Map<String, Object>>> releaseEscrow(@PathVariable String escrowId) {
        return ResponseEntity.ok(ApiResponse.success(Map.of("escrowId", escrowId, "status", "RELEASED"), "Escrow released successfully"));
    }

    @PostMapping("/{escrowId}/hold")
    public ResponseEntity<ApiResponse<Map<String, Object>>> holdEscrow(@PathVariable String escrowId) {
        return ResponseEntity.ok(ApiResponse.success(Map.of("escrowId", escrowId, "status", "ON_HOLD"), "Escrow put on hold successfully"));
    }

    @PostMapping("/{escrowId}/refund")
    public ResponseEntity<ApiResponse<Map<String, Object>>> refundEscrow(@PathVariable String escrowId) {
        return ResponseEntity.ok(ApiResponse.success(Map.of("escrowId", escrowId, "status", "REFUNDED"), "Escrow refunded successfully"));
    }
}
