package com.kfpcl.service;

import com.kfpcl.dto.InventoryAdjustmentDto;
import com.kfpcl.dto.InventoryResponseDto;
import com.kfpcl.dto.PageResponseDto;

public interface SellerInventoryService {
    PageResponseDto<InventoryResponseDto> getSellerInventory(String sellerId, int page, int size);
    InventoryResponseDto adjustStock(String sellerId, String inventoryId, InventoryAdjustmentDto adjustmentDto);
}
