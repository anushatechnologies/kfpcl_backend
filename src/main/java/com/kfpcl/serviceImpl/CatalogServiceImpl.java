package com.kfpcl.serviceImpl;

import com.kfpcl.dto.*;
import com.kfpcl.entity.Category;
import com.kfpcl.entity.Product;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.CategoryRepository;
import com.kfpcl.repository.ProductRepository;
import com.kfpcl.service.CatalogService;
import com.kfpcl.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories(Boolean isActive) {
        List<Category> categories;
        if (Boolean.TRUE.equals(isActive)) {
            categories = categoryRepository.findByStatus(Category.Status.ACTIVE);
        } else {
            categories = categoryRepository.findAll();
        }

        return categories.stream()
                .map(cat -> CategoryResponse.builder()
                        .id(cat.getId())
                        .name(cat.getName())
                        .status(cat.getStatus().name())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getFeaturedProducts() {
        List<Product> products = productRepository.findByFeaturedTrueAndStatus(Product.Status.ACTIVE);
        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getProducts(
            String search,
            String categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer moqMax,
            Boolean gstVerified,
            Boolean verifiedSupplier,
            String sortBy,
            int page,
            int limit,
            Boolean featured
    ) {
        int pageIndex = Math.max(0, page - 1); // 1-based to 0-based for Spring Data PageRequest
        int pageSize = limit > 0 ? limit : 20;

        Sort sort = resolveSort(sortBy);
        Pageable pageable = PageRequest.of(pageIndex, pageSize, sort);

        Specification<Product> spec = ProductSpecification.filter(
                search,
                categoryId,
                minPrice,
                maxPrice,
                moqMax,
                gstVerified,
                verifiedSupplier,
                featured,
                Product.Status.ACTIVE
        );

        Page<Product> productPage = productRepository.findAll(spec, pageable);
        List<ProductResponse> dtos = productPage.getContent().stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());

        return PageResponse.<ProductResponse>builder()
                .content(dtos)
                .page(pageIndex + 1)
                .limit(pageSize)
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .last(productPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductById(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        SupplierSummaryDto supplierDto = null;
        if (product.getSupplier() != null) {
            supplierDto = SupplierSummaryDto.builder()
                    .id(product.getSupplier().getId())
                    .companyName(product.getSupplier().getCompanyName())
                    .gstVerified(product.getSupplier().getGstVerified())
                    .isVerified(product.getSupplier().getIsVerified())
                    .build();
        }

        return ProductDetailResponse.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .supplier(supplierDto)
                .price(product.getPrice())
                .unit(product.getUnit())
                .moq(product.getMoq())
                .stockQuantity(product.getStockQuantity())
                .featured(product.getFeatured())
                .status(product.getStatus().name())
                .imageUrl(product.getImageUrl())
                .gstRate(product.getGstRate())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private ProductResponse mapToProductResponse(Product product) {
        SupplierSummaryDto supplierDto = null;
        if (product.getSupplier() != null) {
            supplierDto = SupplierSummaryDto.builder()
                    .id(product.getSupplier().getId())
                    .companyName(product.getSupplier().getCompanyName())
                    .gstVerified(product.getSupplier().getGstVerified())
                    .isVerified(product.getSupplier().getIsVerified())
                    .build();
        }

        return ProductResponse.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .supplier(supplierDto)
                .price(product.getPrice())
                .unit(product.getUnit())
                .moq(product.getMoq())
                .featured(product.getFeatured())
                .status(product.getStatus().name())
                .imageUrl(product.getImageUrl())
                .build();
    }

    private Sort resolveSort(String sortBy) {
        if (sortBy == null) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        return switch (sortBy.toLowerCase()) {
            case "price_low", "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_high", "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "moq_low" -> Sort.by(Sort.Direction.ASC, "moq");
            case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "title_asc" -> Sort.by(Sort.Direction.ASC, "title");
            case "title_desc" -> Sort.by(Sort.Direction.DESC, "title");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }
}
