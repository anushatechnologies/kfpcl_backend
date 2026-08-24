package com.kfpcl.service;

import com.kfpcl.dto.InventoryAdjustmentDto;
import com.kfpcl.dto.InventoryResponseDto;
import com.kfpcl.dto.InventoryUpdateStockDto;
import com.kfpcl.dto.PageResponseDto;

public interface InventoryService {

    PageResponseDto<InventoryResponseDto> getAllInventory(String status, String search, int page, int size, String sortBy, String sortDir);

    InventoryResponseDto getInventoryById(String inventoryId);

    InventoryResponseDto getInventoryByProductId(String productId);

    InventoryResponseDto updateStock(String inventoryId, InventoryUpdateStockDto dto);

    InventoryResponseDto adjustStock(String inventoryId, InventoryAdjustmentDto dto);
}
