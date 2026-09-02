package com.kfpcl.serviceImpl;

import com.kfpcl.dto.InventoryAdjustmentDto;
import com.kfpcl.dto.InventoryCreateDto;
import com.kfpcl.dto.InventoryLogDto;
import com.kfpcl.dto.InventoryResponseDto;
import com.kfpcl.dto.InventoryUpdateDetailsDto;
import com.kfpcl.dto.InventoryUpdateStockDto;
import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.entity.Inventory;
import com.kfpcl.entity.InventoryLog;
import com.kfpcl.entity.Product;
import com.kfpcl.exception.BusinessValidationException;
import com.kfpcl.exception.DuplicateResourceException;
import com.kfpcl.exception.InvalidRequestException;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.InventoryLogRepository;
import com.kfpcl.repository.InventoryRepository;
import com.kfpcl.repository.ProductRepository;
import com.kfpcl.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final ProductRepository productRepository;

    @Override
    public InventoryResponseDto createInventory(InventoryCreateDto dto) {
        String productId = StringUtils.hasText(dto.getProductId()) ? dto.getProductId().trim() : "prod_" + UUID.randomUUID().toString().substring(0, 8);

        if (StringUtils.hasText(dto.getProductId())) {
            inventoryRepository.findByProductId(dto.getProductId().trim()).ifPresent(existing -> {
                throw new DuplicateResourceException("Inventory for product ID " + dto.getProductId() + " already exists");
            });
        }

        Inventory inventory = Inventory.builder()
                .id("inv_" + UUID.randomUUID().toString().substring(0, 8))
                .productId(productId)
                .sku(dto.getSku().trim())
                .stockQuantity(dto.getStockQuantity() != null ? dto.getStockQuantity() : 0)
                .reservedQuantity(dto.getReservedQuantity() != null ? dto.getReservedQuantity() : 0)
                .reorderLevel(dto.getReorderLevel() != null ? dto.getReorderLevel() : 10)
                .warehouseLocation(StringUtils.hasText(dto.getWarehouseLocation()) ? dto.getWarehouseLocation().trim() : null)
                .status(Inventory.Status.IN_STOCK)
                .build();

        inventory.recalculateStatus();
        inventory = inventoryRepository.save(inventory);

        InventoryLog log = InventoryLog.builder()
                .id("log_" + UUID.randomUUID().toString().substring(0, 8))
                .inventoryId(inventory.getId())
                .productId(inventory.getProductId())
                .adjustmentType(InventoryLog.AdjustmentType.SET)
                .quantity(inventory.getStockQuantity())
                .previousQuantity(0)
                .newQuantity(inventory.getStockQuantity())
                .reason("Initial inventory item creation")
                .adjustedBy("Admin")
                .build();

        inventoryLogRepository.save(log);

        return getInventoryById(inventory.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<InventoryResponseDto> getAllInventory(String status, String search, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Inventory> inventoryPage;

        if (StringUtils.hasText(status)) {
            Inventory.Status invStatus = parseStatus(status);
            inventoryPage = inventoryRepository.findByStatus(invStatus, pageable);
        } else if (StringUtils.hasText(search)) {
            inventoryPage = inventoryRepository.findBySkuContainingIgnoreCase(search.trim(), pageable);
        } else {
            inventoryPage = inventoryRepository.findAll(pageable);
        }

        Map<String, String> productNames = java.util.Collections.emptyMap();
        try {
            productNames = productRepository.findAll().stream()
                    .collect(Collectors.toMap(Product::getId, Product::getProductName, (a, b) -> a));
        } catch (Exception ignored) {
        }

        final Map<String, String> finalProductNames = productNames;
        List<InventoryResponseDto> dtoList = inventoryPage.getContent().stream()
                .map(inv -> mapToDto(inv, finalProductNames.get(inv.getProductId()), null))
                .collect(Collectors.toList());

        return PageResponseDto.from(inventoryPage, dtoList);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponseDto getInventoryById(String inventoryId) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "inventoryId", inventoryId));

        String productName = "Unknown Product";
        try {
            productName = productRepository.findById(inventory.getProductId())
                    .map(Product::getProductName)
                    .orElse("Unknown Product");
        } catch (Exception ignored) {
        }

        List<InventoryLogDto> logs = inventoryLogRepository.findByInventoryIdOrderByCreatedAtDesc(inventoryId).stream()
                .limit(20)
                .map(this::mapLogToDto)
                .collect(Collectors.toList());

        return mapToDto(inventory, productName, logs);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponseDto getInventoryByProductId(String productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", productId));

        String productName = "Unknown Product";
        try {
            productName = productRepository.findById(productId)
                    .map(Product::getProductName)
                    .orElse("Unknown Product");
        } catch (Exception ignored) {
        }

        List<InventoryLogDto> logs = inventoryLogRepository.findByInventoryIdOrderByCreatedAtDesc(inventory.getId()).stream()
                .limit(20)
                .map(this::mapLogToDto)
                .collect(Collectors.toList());

        return mapToDto(inventory, productName, logs);
    }

    @Override
    public InventoryResponseDto updateStock(String inventoryId, InventoryUpdateStockDto dto) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "inventoryId", inventoryId));

        if (dto.getStockQuantity() < 0) {
            throw new BusinessValidationException("Stock quantity cannot be negative: " + dto.getStockQuantity());
        }

        int previousQty = inventory.getStockQuantity();
        int newQty = dto.getStockQuantity();

        inventory.setStockQuantity(newQty);
        inventory.recalculateStatus();
        inventory = inventoryRepository.save(inventory);

        // Sync Product stock
        try {
            productRepository.findById(inventory.getProductId()).ifPresent(product -> {
                product.setStockQuantity(newQty);
                if (newQty <= 0) {
                    product.setStatus(Product.Status.OUT_OF_STOCK);
                } else if (product.getStatus() == Product.Status.OUT_OF_STOCK) {
                    product.setStatus(Product.Status.ACTIVE);
                }
                productRepository.save(product);
            });
        } catch (Exception ignored) {
        }

        // Create audit movement log
        InventoryLog log = InventoryLog.builder()
                .id("log_" + UUID.randomUUID().toString().substring(0, 8))
                .inventoryId(inventory.getId())
                .productId(inventory.getProductId())
                .adjustmentType(InventoryLog.AdjustmentType.SET)
                .quantity(newQty - previousQty)
                .previousQuantity(previousQty)
                .newQuantity(newQty)
                .reason(StringUtils.hasText(dto.getReason()) ? dto.getReason() : "Manual stock quantity update")
                .adjustedBy("Admin")
                .build();

        inventoryLogRepository.save(log);

        return getInventoryById(inventoryId);
    }

    @Override
    public InventoryResponseDto updateInventoryDetails(String inventoryId, InventoryUpdateDetailsDto dto) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "inventoryId", inventoryId));

        int previousQty = inventory.getStockQuantity();

        if (dto.getStockQuantity() != null) {
            if (dto.getStockQuantity() < 0) {
                throw new BusinessValidationException("Stock quantity cannot be negative: " + dto.getStockQuantity());
            }
            inventory.setStockQuantity(dto.getStockQuantity());
        }

        if (dto.getReservedQuantity() != null) {
            if (dto.getReservedQuantity() < 0) {
                throw new BusinessValidationException("Reserved quantity cannot be negative: " + dto.getReservedQuantity());
            }
            inventory.setReservedQuantity(dto.getReservedQuantity());
        }

        if (dto.getReorderLevel() != null) {
            if (dto.getReorderLevel() < 0) {
                throw new BusinessValidationException("Reorder level cannot be negative: " + dto.getReorderLevel());
            }
            inventory.setReorderLevel(dto.getReorderLevel());
        }

        if (StringUtils.hasText(dto.getWarehouseLocation())) {
            inventory.setWarehouseLocation(dto.getWarehouseLocation().trim());
        }

        if (StringUtils.hasText(dto.getSku())) {
            inventory.setSku(dto.getSku().trim());
        }

        inventory.recalculateStatus();
        inventory = inventoryRepository.save(inventory);

        int newQty = inventory.getStockQuantity();
        if (newQty != previousQty) {
            try {
                productRepository.findById(inventory.getProductId()).ifPresent(product -> {
                    product.setStockQuantity(newQty);
                    if (newQty <= 0) {
                        product.setStatus(Product.Status.OUT_OF_STOCK);
                    } else if (product.getStatus() == Product.Status.OUT_OF_STOCK) {
                        product.setStatus(Product.Status.ACTIVE);
                    }
                    productRepository.save(product);
                });
            } catch (Exception ignored) {
            }

            InventoryLog log = InventoryLog.builder()
                    .id("log_" + UUID.randomUUID().toString().substring(0, 8))
                    .inventoryId(inventory.getId())
                    .productId(inventory.getProductId())
                    .adjustmentType(InventoryLog.AdjustmentType.SET)
                    .quantity(newQty - previousQty)
                    .previousQuantity(previousQty)
                    .newQuantity(newQty)
                    .reason(StringUtils.hasText(dto.getReason()) ? dto.getReason() : "Full inventory details update")
                    .adjustedBy("Admin")
                    .build();

            inventoryLogRepository.save(log);
        }

        return getInventoryById(inventoryId);
    }

    @Override
    public InventoryResponseDto adjustStock(String inventoryId, InventoryAdjustmentDto dto) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "inventoryId", inventoryId));

        InventoryLog.AdjustmentType adjustmentType;
        try {
            adjustmentType = InventoryLog.AdjustmentType.valueOf(dto.getType().toUpperCase().trim());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidRequestException("Invalid adjustmentType: " + dto.getType() + ". Allowed: ADD, SUBTRACT, SET, CORRECTION, DAMAGE, SALE, RETURN");
        }

        int previousQty = inventory.getStockQuantity();
        int newQty;
        int delta = dto.getQuantity();

        switch (adjustmentType) {
            case ADD:
            case RETURN:
                newQty = previousQty + delta;
                break;
            case SUBTRACT:
            case DAMAGE:
            case SALE:
                newQty = previousQty - delta;
                if (newQty < 0) {
                    throw new BusinessValidationException(String.format("Insufficient stock for deduction: Available: %d, Requested deduction: %d", previousQty, delta));
                }
                break;
            case SET:
            case CORRECTION:
                newQty = delta;
                if (newQty < 0) {
                    throw new BusinessValidationException("Stock quantity cannot be set to negative value: " + delta);
                }
                break;
            default:
                newQty = previousQty;
        }

        inventory.setStockQuantity(newQty);
        inventory.recalculateStatus();
        inventoryRepository.save(inventory);

        // Sync Product stock
        try {
            productRepository.findById(inventory.getProductId()).ifPresent(product -> {
                product.setStockQuantity(newQty);
                if (newQty <= 0) {
                    product.setStatus(Product.Status.OUT_OF_STOCK);
                } else if (product.getStatus() == Product.Status.OUT_OF_STOCK) {
                    product.setStatus(Product.Status.ACTIVE);
                }
                productRepository.save(product);
            });
        } catch (Exception ignored) {
        }

        // Record movement log
        InventoryLog log = InventoryLog.builder()
                .id("log_" + UUID.randomUUID().toString().substring(0, 8))
                .inventoryId(inventory.getId())
                .productId(inventory.getProductId())
                .adjustmentType(adjustmentType)
                .quantity(newQty - previousQty)
                .previousQuantity(previousQty)
                .newQuantity(newQty)
                .reason(dto.getReason())
                .adjustedBy(StringUtils.hasText(dto.getAdjustedBy()) ? dto.getAdjustedBy() : "Admin")
                .build();

        inventoryLogRepository.save(log);

        return getInventoryById(inventoryId);
    }

    @Override
    public void deleteInventory(String inventoryId) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "inventoryId", inventoryId));

        inventoryLogRepository.deleteByInventoryId(inventoryId);
        inventoryRepository.delete(inventory);
    }

    private Inventory.Status parseStatus(String statusStr) {
        try {
            return Inventory.Status.valueOf(statusStr.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return Inventory.Status.IN_STOCK;
        }
    }

    private InventoryResponseDto mapToDto(Inventory inventory, String productName, List<InventoryLogDto> logs) {
        int available = Math.max(0, inventory.getStockQuantity() - inventory.getReservedQuantity());

        return InventoryResponseDto.builder()
                .id(inventory.getId())
                .productId(inventory.getProductId())
                .productName(productName)
                .sku(inventory.getSku())
                .stockQuantity(inventory.getStockQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .availableQuantity(available)
                .reorderLevel(inventory.getReorderLevel())
                .warehouseLocation(inventory.getWarehouseLocation())
                .status(inventory.getStatus() != null ? inventory.getStatus().name() : Inventory.Status.IN_STOCK.name())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .recentLogs(logs)
                .build();
    }

    private InventoryLogDto mapLogToDto(InventoryLog log) {
        return InventoryLogDto.builder()
                .id(log.getId())
                .inventoryId(log.getInventoryId())
                .productId(log.getProductId())
                .adjustmentType(log.getAdjustmentType() != null ? log.getAdjustmentType().name() : "")
                .quantity(log.getQuantity())
                .previousQuantity(log.getPreviousQuantity())
                .newQuantity(log.getNewQuantity())
                .reason(log.getReason())
                .adjustedBy(log.getAdjustedBy())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
