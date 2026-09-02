package com.kfpcl.service;

import com.kfpcl.dto.InventoryAdjustmentDto;
import com.kfpcl.dto.InventoryResponseDto;
import com.kfpcl.dto.InventoryUpdateStockDto;
import com.kfpcl.entity.Inventory;
import com.kfpcl.entity.InventoryLog;
import com.kfpcl.entity.Product;
import com.kfpcl.exception.BusinessValidationException;
import com.kfpcl.repository.InventoryLogRepository;
import com.kfpcl.repository.InventoryRepository;
import com.kfpcl.repository.ProductRepository;
import com.kfpcl.serviceImpl.InventoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private InventoryLogRepository inventoryLogRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Inventory inventory;
    private Product product;

    @BeforeEach
    void setUp() {
        inventory = Inventory.builder()
                .id("inv_1001")
                .productId("prod_1001")
                .sku("AML-MILK-500")
                .stockQuantity(50)
                .reservedQuantity(5)
                .reorderLevel(10)
                .status(Inventory.Status.IN_STOCK)
                .build();

        product = Product.builder()
                .id("prod_1001")
                .productName("Amul Milk")
                .sku("AML-MILK-500")
                .stockQuantity(50)
                .status(Product.Status.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Update Stock - Success")
    void testUpdateStock_Success() {
        InventoryUpdateStockDto dto = InventoryUpdateStockDto.builder()
                .stockQuantity(100)
                .reason("Restocked by supplier")
                .build();

        when(inventoryRepository.findById("inv_1001")).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(productRepository.findById("prod_1001")).thenReturn(Optional.of(product));
        when(inventoryLogRepository.findByInventoryIdOrderByCreatedAtDesc("inv_1001")).thenReturn(Collections.emptyList());

        InventoryResponseDto response = inventoryService.updateStock("inv_1001", dto);

        assertNotNull(response);
        assertEquals(100, inventory.getStockQuantity());
        verify(inventoryLogRepository, times(1)).save(any(InventoryLog.class));
    }

    @Test
    @DisplayName("Adjust Stock - Subtract Success")
    void testAdjustStock_SubtractSuccess() {
        InventoryAdjustmentDto dto = InventoryAdjustmentDto.builder()
                .type("SUBTRACT")
                .quantity(20)
                .reason("Damaged during transit")
                .adjustedBy("Admin")
                .build();

        when(inventoryRepository.findById("inv_1001")).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(productRepository.findById("prod_1001")).thenReturn(Optional.of(product));
        when(inventoryLogRepository.findByInventoryIdOrderByCreatedAtDesc("inv_1001")).thenReturn(Collections.emptyList());

        InventoryResponseDto response = inventoryService.adjustStock("inv_1001", dto);

        assertNotNull(response);
        assertEquals(30, inventory.getStockQuantity()); // 50 - 20 = 30
        verify(inventoryLogRepository, times(1)).save(any(InventoryLog.class));
    }

    @Test
    @DisplayName("Adjust Stock - Insufficient Stock Throws BusinessValidationException")
    void testAdjustStock_InsufficientStock() {
        InventoryAdjustmentDto dto = InventoryAdjustmentDto.builder()
                .type("SUBTRACT")
                .quantity(100) // Available is only 50
                .reason("Large withdrawal")
                .build();

        when(inventoryRepository.findById("inv_1001")).thenReturn(Optional.of(inventory));

        assertThrows(BusinessValidationException.class, () -> inventoryService.adjustStock("inv_1001", dto));
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Update Inventory Details - Full Success")
    void testUpdateInventoryDetails_Success() {
        com.kfpcl.dto.InventoryUpdateDetailsDto dto = com.kfpcl.dto.InventoryUpdateDetailsDto.builder()
                .stockQuantity(120)
                .reservedQuantity(15)
                .reorderLevel(25)
                .warehouseLocation("Warehouse B, Kolar")
                .sku("AML-MILK-500-V2")
                .reason("Full inventory detail update")
                .build();

        when(inventoryRepository.findById("inv_1001")).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(productRepository.findById("prod_1001")).thenReturn(Optional.of(product));
        when(inventoryLogRepository.findByInventoryIdOrderByCreatedAtDesc("inv_1001")).thenReturn(Collections.emptyList());

        InventoryResponseDto response = inventoryService.updateInventoryDetails("inv_1001", dto);

        assertNotNull(response);
        assertEquals(120, inventory.getStockQuantity());
        assertEquals(15, inventory.getReservedQuantity());
        assertEquals(25, inventory.getReorderLevel());
        assertEquals("Warehouse B, Kolar", inventory.getWarehouseLocation());
        assertEquals("AML-MILK-500-V2", inventory.getSku());
    }

    @Test
    @DisplayName("Delete Inventory - Success")
    void testDeleteInventory_Success() {
        when(inventoryRepository.findById("inv_1001")).thenReturn(Optional.of(inventory));
        doNothing().when(inventoryLogRepository).deleteByInventoryId("inv_1001");
        doNothing().when(inventoryRepository).delete(inventory);

        inventoryService.deleteInventory("inv_1001");

        verify(inventoryLogRepository, times(1)).deleteByInventoryId("inv_1001");
        verify(inventoryRepository, times(1)).delete(inventory);
    }

    @Test
    @DisplayName("Create Inventory - Success")
    void testCreateInventory_Success() {
        com.kfpcl.dto.InventoryCreateDto dto = com.kfpcl.dto.InventoryCreateDto.builder()
                .productId("prod_2002")
                .sku("AML-MILK-1000")
                .stockQuantity(80)
                .reservedQuantity(10)
                .reorderLevel(15)
                .warehouseLocation("Warehouse Main, Bangalore")
                .build();

        when(inventoryRepository.findByProductId("prod_2002")).thenReturn(Optional.empty());
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(inventoryRepository.findById(anyString())).thenReturn(Optional.of(inventory));

        InventoryResponseDto response = inventoryService.createInventory(dto);

        assertNotNull(response);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
        verify(inventoryLogRepository, times(1)).save(any(InventoryLog.class));
    }
}
