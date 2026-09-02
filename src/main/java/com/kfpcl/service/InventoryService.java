package com.kfpcl.service;

import com.kfpcl.dto.InventoryAdjustmentDto;
import com.kfpcl.dto.InventoryResponseDto;
import com.kfpcl.dto.InventoryUpdateStockDto;
import com.kfpcl.dto.PageResponseDto;

import com.kfpcl.dto.InventoryCreateDto;
import com.kfpcl.dto.InventoryUpdateDetailsDto;

public interface InventoryService {

    InventoryResponseDto createInventory(InventoryCreateDto dto);

    PageResponseDto<InventoryResponseDto> getAllInventory(String status, String search, int page, int size, String sortBy, String sortDir);

    InventoryResponseDto getInventoryById(String inventoryId);

    InventoryResponseDto getInventoryByProductId(String productId);

    InventoryResponseDto updateStock(String inventoryId, InventoryUpdateStockDto dto);

    InventoryResponseDto updateInventoryDetails(String inventoryId, InventoryUpdateDetailsDto dto);

    InventoryResponseDto adjustStock(String inventoryId, InventoryAdjustmentDto dto);

    void deleteInventory(String inventoryId);
}
