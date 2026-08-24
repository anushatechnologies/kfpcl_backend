package com.kfpcl.controller;

import com.kfpcl.dto.*;
import com.kfpcl.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/inventory")
@RequiredArgsConstructor
public class AdminInventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDto<InventoryResponseDto>>> listInventory(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        PageResponseDto<InventoryResponseDto> inventory = inventoryService.getAllInventory(status, search, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(inventory, "Inventory list retrieved successfully"));
    }

    @GetMapping("/{inventoryId}")
    public ResponseEntity<ApiResponse<InventoryResponseDto>> getInventoryDetails(
            @PathVariable String inventoryId) {

        InventoryResponseDto inventory = inventoryService.getInventoryById(inventoryId);
        return ResponseEntity.ok(ApiResponse.success(inventory, "Inventory details retrieved successfully"));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<InventoryResponseDto>> getInventoryByProduct(
            @PathVariable String productId) {

        InventoryResponseDto inventory = inventoryService.getInventoryByProductId(productId);
        return ResponseEntity.ok(ApiResponse.success(inventory, "Product inventory retrieved successfully"));
    }

    @PatchMapping("/{inventoryId}/stock")
    public ResponseEntity<ApiResponse<InventoryResponseDto>> updateStock(
            @PathVariable String inventoryId,
            @Valid @RequestBody InventoryUpdateStockDto dto) {

        InventoryResponseDto updated = inventoryService.updateStock(inventoryId, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Inventory stock quantity updated successfully"));
    }

    @PostMapping("/{inventoryId}/adjustment")
    public ResponseEntity<ApiResponse<InventoryResponseDto>> adjustStock(
            @PathVariable String inventoryId,
            @Valid @RequestBody InventoryAdjustmentDto dto) {

        InventoryResponseDto adjusted = inventoryService.adjustStock(inventoryId, dto);
        return ResponseEntity.ok(ApiResponse.success(adjusted, "Inventory stock adjusted successfully"));
    }
}
