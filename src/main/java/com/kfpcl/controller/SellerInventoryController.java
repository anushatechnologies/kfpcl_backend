package com.kfpcl.controller;

import com.kfpcl.dto.ApiResponse;
import com.kfpcl.dto.InventoryAdjustmentDto;
import com.kfpcl.dto.InventoryResponseDto;
import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.service.SellerInventoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/seller/inventory")
@RequiredArgsConstructor
public class SellerInventoryController {

    private final SellerInventoryService sellerInventoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDto<InventoryResponseDto>>> getMyInventory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        String sellerId = (String) request.getAttribute("authenticatedUser");
        PageResponseDto<InventoryResponseDto> responses = sellerInventoryService.getSellerInventory(sellerId, page, size);
        return ResponseEntity.ok(ApiResponse.success(responses, "Inventory retrieved successfully"));
    }

    @PatchMapping("/{inventoryId}/stock")
    public ResponseEntity<ApiResponse<InventoryResponseDto>> adjustStock(
            @PathVariable String inventoryId,
            @RequestBody InventoryAdjustmentDto adjustmentDto,
            HttpServletRequest request) {

        String sellerId = (String) request.getAttribute("authenticatedUser");
        InventoryResponseDto response = sellerInventoryService.adjustStock(sellerId, inventoryId, adjustmentDto);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock adjusted successfully"));
    }
}
