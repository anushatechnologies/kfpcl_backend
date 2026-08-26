package com.kfpcl.serviceImpl;

import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.dto.ProductCreateDto;
import com.kfpcl.dto.ProductResponseDto;
import com.kfpcl.dto.ProductUpdateDto;
import com.kfpcl.dto.SellerProductCreateDto;
import com.kfpcl.entity.*;
import com.kfpcl.exception.BusinessValidationException;
import com.kfpcl.exception.DuplicateResourceException;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.*;
import com.kfpcl.service.ProductService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryLogRepository inventoryLogRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<ProductResponseDto> getBuyerProducts(String search, String categoryId, String subcategoryId, String brand, Double minPrice, Double maxPrice, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), Product.Status.ACTIVE));
            predicates.add(cb.equal(root.get("approvalStatus"), Product.ApprovalStatus.APPROVED));

            if (StringUtils.hasText(search)) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("productName")), pattern);
                Predicate brandMatch = cb.like(cb.lower(root.get("brand")), pattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), pattern);
                predicates.add(cb.or(nameMatch, brandMatch, descMatch));
            }
            if (StringUtils.hasText(categoryId)) {
                predicates.add(cb.equal(root.get("categoryId"), categoryId.trim()));
            }
            if (StringUtils.hasText(subcategoryId)) {
                predicates.add(cb.equal(root.get("subcategoryId"), subcategoryId.trim()));
            }
            if (StringUtils.hasText(brand)) {
                predicates.add(cb.equal(cb.lower(root.get("brand")), brand.trim().toLowerCase()));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Product> productPage = productRepository.findAll(spec, pageable);
        return mapToPageResponse(productPage);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getBuyerProductById(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        if (!product.isActive()) {
            throw new ResourceNotFoundException("Product is currently unavailable or inactive: " + productId);
        }

        return mapToDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<ProductResponseDto> getAdminProducts(String search, String categoryId, String subcategoryId, String status, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(search)) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("productName")), pattern);
                Predicate skuMatch = cb.like(cb.lower(root.get("sku")), pattern);
                Predicate brandMatch = cb.like(cb.lower(root.get("brand")), pattern);
                predicates.add(cb.or(nameMatch, skuMatch, brandMatch));
            }
            if (StringUtils.hasText(categoryId)) {
                predicates.add(cb.equal(root.get("categoryId"), categoryId.trim()));
            }
            if (StringUtils.hasText(subcategoryId)) {
                predicates.add(cb.equal(root.get("subcategoryId"), subcategoryId.trim()));
            }
            if (StringUtils.hasText(status)) {
                Product.Status prodStatus = parseStatus(status);
                predicates.add(cb.equal(root.get("status"), prodStatus));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Product> productPage = productRepository.findAll(spec, pageable);
        return mapToPageResponse(productPage);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getAdminProductById(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        return mapToDto(product);
    }

    @Override
    public ProductResponseDto createProduct(ProductCreateDto dto) {
        // 1. Validate Category & Subcategory relationship
        Category category = categoryRepository.findById(dto.getCategoryId().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", dto.getCategoryId()));

        if (!category.isActive()) {
            throw new BusinessValidationException("Cannot create product under an inactive or archived category: " + category.getName());
        }

        Subcategory subcategory = subcategoryRepository.findById(dto.getSubcategoryId().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Subcategory", "subcategoryId", dto.getSubcategoryId()));

        if (!subcategory.getCategoryId().equals(category.getId())) {
            throw new BusinessValidationException(String.format("Subcategory '%s' does not belong to Category '%s'", subcategory.getName(), category.getName()));
        }

        if (!subcategory.isActive()) {
            throw new BusinessValidationException("Cannot create product under an inactive or archived subcategory: " + subcategory.getName());
        }

        // 2. Validate SKU Uniqueness
        if (productRepository.existsBySku(dto.getSku().trim())) {
            throw new DuplicateResourceException("Product", "sku", dto.getSku());
        }

        // 3. Validate Price & MRP Rules
        if (dto.getPrice() > dto.getMrp()) {
            throw new BusinessValidationException(String.format("Product price (%.2f) cannot exceed MRP (%.2f)", dto.getPrice(), dto.getMrp()));
        }

        // 4. Calculate server-side discount percentage
        double calculatedDiscount = calculateDiscount(dto.getPrice(), dto.getMrp());

        // 5. Generate Product ID if not provided
        String productId = StringUtils.hasText(dto.getId())
                ? dto.getId().trim()
                : "prod_" + slugify(dto.getProductName()) + "_" + UUID.randomUUID().toString().substring(0, 6);

        int initialStock = dto.getStockQuantity() != null ? dto.getStockQuantity() : 0;
        Product.Status status = parseStatus(dto.getStatus());
        if (initialStock <= 0 && status == Product.Status.ACTIVE) {
            // Optional: retain ACTIVE or mark IN_STOCK per requirements
        }

        Product product = Product.builder()
                .id(productId)
                .productName(dto.getProductName().trim())
                .categoryId(category.getId())
                .subcategoryId(subcategory.getId())
                .brand(dto.getBrand())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                .price(dto.getPrice())
                .mrp(dto.getMrp())
                .quantity(dto.getQuantity())
                .unit(dto.getUnit())
                .stockQuantity(initialStock)
                .status(status)
                .approvalStatus(Product.ApprovalStatus.APPROVED)
                .createdBy("ADMIN")
                .sku(dto.getSku().trim())
                .discount(calculatedDiscount)
                .build();

        Product savedProduct = productRepository.save(product);

        // 6. Initialize Inventory & InventoryLog
        String inventoryId = "inv_" + UUID.randomUUID().toString().substring(0, 8);
        Inventory.Status invStatus = initialStock > 0 ? Inventory.Status.IN_STOCK : Inventory.Status.OUT_OF_STOCK;

        Inventory inventory = Inventory.builder()
                .id(inventoryId)
                .productId(savedProduct.getId())
                .sku(savedProduct.getSku())
                .stockQuantity(initialStock)
                .reservedQuantity(0)
                .reorderLevel(10)
                .status(invStatus)
                .build();

        inventory.recalculateStatus();
        inventoryRepository.save(inventory);

        InventoryLog log = InventoryLog.builder()
                .id("log_" + UUID.randomUUID().toString().substring(0, 8))
                .inventoryId(inventory.getId())
                .productId(savedProduct.getId())
                .adjustmentType(InventoryLog.AdjustmentType.INITIAL)
                .quantity(initialStock)
                .previousQuantity(0)
                .newQuantity(initialStock)
                .reason("Initial stock setup upon product creation")
                .adjustedBy("Admin")
                .build();

        inventoryLogRepository.save(log);

        return mapToDto(savedProduct, category.getName(), subcategory.getName());
    }

    @Override
    public ProductResponseDto submitSellerProduct(SellerProductCreateDto dto) {
        // 1. Validate Category & Subcategory relationship
        Category category = categoryRepository.findById(dto.getCategoryId().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", dto.getCategoryId()));

        if (!category.isActive()) {
            throw new BusinessValidationException("Cannot submit product under an inactive or archived category: " + category.getName());
        }

        Subcategory subcategory = subcategoryRepository.findById(dto.getSubcategoryId().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Subcategory", "subcategoryId", dto.getSubcategoryId()));

        if (!subcategory.getCategoryId().equals(category.getId())) {
            throw new BusinessValidationException(String.format("Subcategory '%s' does not belong to Category '%s'", subcategory.getName(), category.getName()));
        }

        if (!subcategory.isActive()) {
            throw new BusinessValidationException("Cannot submit product under an inactive or archived subcategory: " + subcategory.getName());
        }

        // 2. Validate SKU Uniqueness
        if (productRepository.existsBySku(dto.getSku().trim())) {
            throw new DuplicateResourceException("Product", "sku", dto.getSku());
        }

        // 3. Validate Price & MRP Rules
        if (dto.getPrice() > dto.getMrp()) {
            throw new BusinessValidationException(String.format("Product price (%.2f) cannot exceed MRP (%.2f)", dto.getPrice(), dto.getMrp()));
        }

        double calculatedDiscount = calculateDiscount(dto.getPrice(), dto.getMrp());

        String productId = StringUtils.hasText(dto.getId())
                ? dto.getId().trim()
                : "prod_" + slugify(dto.getProductName()) + "_" + UUID.randomUUID().toString().substring(0, 6);

        int initialStock = dto.getStockQuantity() != null ? dto.getStockQuantity() : 0;

        // Seller submitted products require admin approval (PENDING & INACTIVE)
        Product product = Product.builder()
                .id(productId)
                .productName(dto.getProductName().trim())
                .categoryId(category.getId())
                .subcategoryId(subcategory.getId())
                .brand(dto.getBrand())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                .price(dto.getPrice())
                .mrp(dto.getMrp())
                .quantity(dto.getQuantity())
                .unit(dto.getUnit())
                .stockQuantity(initialStock)
                .status(Product.Status.INACTIVE)
                .approvalStatus(Product.ApprovalStatus.PENDING)
                .sellerId(dto.getSellerId().trim())
                .createdBy("SELLER")
                .sku(dto.getSku().trim())
                .discount(calculatedDiscount)
                .build();

        Product savedProduct = productRepository.save(product);

        // Initialize Inventory in PENDING state
        String inventoryId = "inv_" + UUID.randomUUID().toString().substring(0, 8);
        Inventory inventory = Inventory.builder()
                .id(inventoryId)
                .productId(savedProduct.getId())
                .sku(savedProduct.getSku())
                .stockQuantity(initialStock)
                .reservedQuantity(0)
                .reorderLevel(10)
                .status(Inventory.Status.OUT_OF_STOCK)
                .build();
        inventoryRepository.save(inventory);

        return mapToDto(savedProduct, category.getName(), subcategory.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<ProductResponseDto> getSellerProducts(String sellerId, String approvalStatus, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(sellerId)) {
                predicates.add(cb.equal(root.get("sellerId"), sellerId.trim()));
            }
            if (StringUtils.hasText(approvalStatus)) {
                try {
                    Product.ApprovalStatus appStatus = Product.ApprovalStatus.valueOf(approvalStatus.trim().toUpperCase());
                    predicates.add(cb.equal(root.get("approvalStatus"), appStatus));
                } catch (IllegalArgumentException ignored) {}
            }
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Product> productPage = productRepository.findAll(spec, pageable);
        return mapToPageResponse(productPage);
    }

    @Override
    public ProductResponseDto updateProduct(String productId, ProductUpdateDto dto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        if (StringUtils.hasText(dto.getSku())) {
            if (productRepository.existsBySkuAndIdNot(dto.getSku().trim(), productId)) {
                throw new DuplicateResourceException("Product", "sku", dto.getSku());
            }
            product.setSku(dto.getSku().trim());
        }

        if (StringUtils.hasText(dto.getCategoryId())) {
            Category category = categoryRepository.findById(dto.getCategoryId().trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", dto.getCategoryId()));
            product.setCategoryId(category.getId());
        }

        if (StringUtils.hasText(dto.getSubcategoryId())) {
            Subcategory subcategory = subcategoryRepository.findById(dto.getSubcategoryId().trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Subcategory", "subcategoryId", dto.getSubcategoryId()));

            if (!subcategory.getCategoryId().equals(product.getCategoryId())) {
                throw new BusinessValidationException(String.format("Subcategory '%s' does not belong to Category '%s'", subcategory.getName(), product.getCategoryId()));
            }
            product.setSubcategoryId(subcategory.getId());
        }

        if (StringUtils.hasText(dto.getProductName())) {
            product.setProductName(dto.getProductName().trim());
        }
        if (dto.getBrand() != null) {
            product.setBrand(dto.getBrand());
        }
        if (dto.getDescription() != null) {
            product.setDescription(dto.getDescription());
        }
        if (dto.getImageUrl() != null) {
            product.setImageUrl(dto.getImageUrl());
        }
        if (dto.getQuantity() != null) {
            product.setQuantity(dto.getQuantity());
        }
        if (dto.getUnit() != null) {
            product.setUnit(dto.getUnit());
        }

        double price = dto.getPrice() != null ? dto.getPrice() : product.getPrice();
        double mrp = dto.getMrp() != null ? dto.getMrp() : product.getMrp();

        if (price > mrp) {
            throw new BusinessValidationException(String.format("Product price (%.2f) cannot exceed MRP (%.2f)", price, mrp));
        }

        product.setPrice(price);
        product.setMrp(mrp);
        product.setDiscount(calculateDiscount(price, mrp));

        if (StringUtils.hasText(dto.getStatus())) {
            product.setStatus(parseStatus(dto.getStatus()));
        }

        Product updated = productRepository.save(product);

        // Sync SKU in Inventory if changed
        inventoryRepository.findByProductId(productId).ifPresent(inv -> {
            if (!inv.getSku().equals(updated.getSku())) {
                inv.setSku(updated.getSku());
                inventoryRepository.save(inv);
            }
        });

        return mapToDto(updated);
    }

    @Override
    public void deleteProduct(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        product.setStatus(Product.Status.ARCHIVED);
        productRepository.save(product);
    }

    private double calculateDiscount(double price, double mrp) {
        if (mrp <= 0 || price >= mrp) {
            return 0.0;
        }
        double discount = ((mrp - price) / mrp) * 100.0;
        return BigDecimal.valueOf(discount)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private Product.Status parseStatus(String statusStr) {
        if (!StringUtils.hasText(statusStr)) {
            return Product.Status.ACTIVE;
        }
        try {
            return Product.Status.valueOf(statusStr.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return Product.Status.ACTIVE;
        }
    }

    private String slugify(String input) {
        return input.toLowerCase()
                .replaceAll("[^a-z0-9]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
    }

    private PageResponseDto<ProductResponseDto> mapToPageResponse(Page<Product> productPage) {
        Map<String, String> categoryNames = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));

        Map<String, String> subcategoryNames = subcategoryRepository.findAll().stream()
                .collect(Collectors.toMap(Subcategory::getId, Subcategory::getName, (a, b) -> a));

        List<ProductResponseDto> dtoList = productPage.getContent().stream()
                .map(p -> mapToDto(p, categoryNames.get(p.getCategoryId()), subcategoryNames.get(p.getSubcategoryId())))
                .collect(Collectors.toList());

        return PageResponseDto.from(productPage, dtoList);
    }

    private ProductResponseDto mapToDto(Product product) {
        String catName = categoryRepository.findById(product.getCategoryId())
                .map(Category::getName)
                .orElse("Unknown");

        String subName = subcategoryRepository.findById(product.getSubcategoryId())
                .map(Subcategory::getName)
                .orElse("Unknown");

        return mapToDto(product, catName, subName);
    }

    private ProductResponseDto mapToDto(Product product, String categoryName, String subcategoryName) {
        return ProductResponseDto.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .categoryId(product.getCategoryId())
                .categoryName(categoryName)
                .subcategoryId(product.getSubcategoryId())
                .subcategoryName(subcategoryName)
                .brand(product.getBrand())
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .price(product.getPrice())
                .mrp(product.getMrp())
                .quantity(product.getQuantity())
                .unit(product.getUnit())
                .stockQuantity(product.getStockQuantity())
                .status(product.getStatus() != null ? product.getStatus().name() : Product.Status.ACTIVE.name())
                .approvalStatus(product.getApprovalStatus() != null ? product.getApprovalStatus().name() : Product.ApprovalStatus.APPROVED.name())
                .rejectionReason(product.getRejectionReason())
                .sellerId(product.getSellerId())
                .createdBy(product.getCreatedBy() != null ? product.getCreatedBy() : "ADMIN")
                .sku(product.getSku())
                .discount(product.getDiscount())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
