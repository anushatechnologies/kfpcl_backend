package com.kfpcl.controller;

import com.kfpcl.dto.*;
import com.kfpcl.service.AdminBuyerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/buyers")
@RequiredArgsConstructor
public class AdminBuyerController {

    private final AdminBuyerService adminBuyerService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDto<AdminUserResponseDto>>> listBuyers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        PageResponseDto<AdminUserResponseDto> buyers = adminBuyerService.getBuyers(search, status, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(buyers, "Buyers retrieved successfully"));
    }

    @GetMapping("/{buyerId}")
    public ResponseEntity<ApiResponse<BuyerDetailResponseDto>> getBuyer(@PathVariable String buyerId) {
        BuyerDetailResponseDto buyer = adminBuyerService.getBuyerById(buyerId);
        return ResponseEntity.ok(ApiResponse.success(buyer, "Buyer details retrieved successfully"));
    }

    @GetMapping("/{buyerId}/activity")
    public ResponseEntity<ApiResponse<List<BuyerActivityDto>>> getBuyerActivity(@PathVariable String buyerId) {
        List<BuyerActivityDto> activity = adminBuyerService.getBuyerActivity(buyerId);
        return ResponseEntity.ok(ApiResponse.success(activity, "Buyer activity retrieved successfully"));
    }

    @PatchMapping("/{buyerId}/status")
    public ResponseEntity<ApiResponse<BuyerDetailResponseDto>> updateBuyerStatus(
            @PathVariable String buyerId,
            @Valid @RequestBody UserStatusUpdateDto dto) {

        BuyerDetailResponseDto updated = adminBuyerService.updateBuyerStatus(buyerId, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Buyer status updated successfully"));
    }
}
