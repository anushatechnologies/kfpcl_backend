package com.kfpcl.serviceImpl;

import com.kfpcl.dto.InventoryAdjustmentDto;
import com.kfpcl.dto.InventoryResponseDto;
import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.entity.Inventory;
import com.kfpcl.entity.InventoryLog;
import com.kfpcl.entity.Product;
import com.kfpcl.exception.BusinessValidationException;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.InventoryLogRepository;
import com.kfpcl.repository.InventoryRepository;
import com.kfpcl.repository.ProductRepository;
import com.kfpcl.service.SellerInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerInventoryServiceImpl implements SellerInventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final InventoryLogRepository inventoryLogRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<InventoryResponseDto> getSellerInventory(String sellerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Inventory> inventoryPage = inventoryRepository.findBySellerId(sellerId, pageable);

        List<InventoryResponseDto> content = inventoryPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponseDto.<InventoryResponseDto>builder()
                .content(content)
                .page(inventoryPage.getNumber())
                .size(inventoryPage.getSize())
                .totalElements(inventoryPage.getTotalElements())
                .totalPages(inventoryPage.getTotalPages())
                .isLast(inventoryPage.isLast())
                .build();
    }

    @Override
    @Transactional
    public InventoryResponseDto adjustStock(String sellerId, String inventoryId, InventoryAdjustmentDto adjustmentDto) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory record not found"));

        Product product = productRepository.findById(inventory.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!product.getSellerId().equals(sellerId)) {
            throw new IllegalArgumentException("Unauthorized to adjust stock for this product");
        }

        int previousStock = inventory.getStockQuantity();
        int newStock = previousStock;

        switch (adjustmentDto.getType()) {
            case "ADD":
                newStock += adjustmentDto.getQuantity();
                break;
            case "SUBTRACT":
                if (previousStock < adjustmentDto.getQuantity()) {
                    throw new BusinessValidationException("Insufficient stock for subtraction");
                }
                newStock -= adjustmentDto.getQuantity();
                break;
            case "SET":
                if (adjustmentDto.getQuantity() < 0) {
                    throw new BusinessValidationException("Stock quantity cannot be negative");
                }
                newStock = adjustmentDto.getQuantity();
                break;
            default:
                throw new IllegalArgumentException("Invalid adjustment type. Use ADD, SUBTRACT, or SET");
        }

        inventory.setStockQuantity(newStock);
        inventory.recalculateStatus();
        inventory = inventoryRepository.save(inventory);

        InventoryLog log = InventoryLog.builder()
                .id(UUID.randomUUID().toString())
                .inventoryId(inventoryId)
                .productId(inventory.getProductId())
                .adjustmentType(InventoryLog.AdjustmentType.valueOf(adjustmentDto.getType()))
                .quantity(adjustmentDto.getQuantity())
                .previousQuantity(previousStock)
                .newQuantity(newStock)
                .reason(adjustmentDto.getReason())
                .adjustedBy(sellerId)
                .build();
        inventoryLogRepository.save(log);

        return mapToResponse(inventory);
    }

    private InventoryResponseDto mapToResponse(Inventory inventory) {
        Product product = productRepository.findById(inventory.getProductId()).orElse(null);
        String productName = product != null ? product.getProductName() : null;

        return InventoryResponseDto.builder()
                .id(inventory.getId())
                .productId(inventory.getProductId())
                .productName(productName)
                .sku(inventory.getSku())
                .stockQuantity(inventory.getStockQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .availableQuantity(inventory.getStockQuantity() - inventory.getReservedQuantity())
                .reorderLevel(inventory.getReorderLevel())
                .status(inventory.getStatus().name())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}
