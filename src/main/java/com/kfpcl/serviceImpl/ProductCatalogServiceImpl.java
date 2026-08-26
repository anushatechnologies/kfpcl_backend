package com.kfpcl.serviceImpl;

import com.kfpcl.dto.request.ProductFilterRequest;
import com.kfpcl.dto.response.PageResponse;
import com.kfpcl.dto.response.ProductPriceTierResponse;
import com.kfpcl.dto.response.ProductResponse;
import com.kfpcl.entity.Product;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.ProductRepository;
import com.kfpcl.service.ProductCatalogService;
import com.kfpcl.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductCatalogServiceImpl implements ProductCatalogService {

    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> searchProducts(
            ProductFilterRequest filter,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        String sanitizedSortBy = sanitizeSortField(sortBy);
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sanitizedSortBy).ascending()
                : Sort.by(sanitizedSortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<Product> spec = ProductSpecification.filterPublicProducts(filter);

        Page<Product> productPage = productRepository.findAll(spec, pageable);

        List<ProductResponse> mapped = productPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.from(productPage, mapped);
    }

    @Override
    @Transactional
    public ProductResponse getProductDetailsById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (!product.isPubliclyVisible()) {
            throw new ResourceNotFoundException("Product", "id", id);
        }

        // Increment view count for discovery analytics
        product.setViewCount(product.getViewCount() + 1);
        productRepository.save(product);

        return mapToResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse getProductDetailsBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "slug", slug));

        if (!product.isPubliclyVisible()) {
            throw new ResourceNotFoundException("Product", "slug", slug);
        }

        // Increment view count
        product.setViewCount(product.getViewCount() + 1);
        productRepository.save(product);

        return mapToResponse(product);
    }

    private String sanitizeSortField(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "createdAt";
        }
        return switch (sortBy.toLowerCase()) {
            case "price", "baseprice" -> "basePrice";
            case "name" -> "name";
            case "views", "viewcount" -> "viewCount";
            case "moq" -> "moq";
            default -> "createdAt";
        };
    }

    private ProductResponse mapToResponse(Product product) {
        if (product == null) {
            return null;
        }

        List<ProductPriceTierResponse> tiersResponse = null;
        if (product.getPriceTiers() != null && !product.getPriceTiers().isEmpty()) {
            tiersResponse = product.getPriceTiers().stream()
                    .map(tier -> ProductPriceTierResponse.builder()
                            .id(tier.getId())
                            .minQuantity(tier.getMinQuantity())
                            .maxQuantity(tier.getMaxQuantity())
                            .price(tier.getPrice())
                            .build())
                    .collect(Collectors.toList());
        }

        return ProductResponse.builder()
                .id(product.getId())
                .sellerId(product.getSeller() != null ? product.getSeller().getId() : null)
                .sellerCompanyName(product.getSeller() != null ? product.getSeller().getCompanyName() : null)
                .sellerIsVerified(product.getSeller() != null ? product.getSeller().getIsVerified() : false)
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .categorySlug(product.getCategory() != null ? product.getCategory().getSlug() : null)
                .name(product.getName())
                .slug(product.getSlug())
                .sku(product.getSku())
                .description(product.getDescription())
                .basePrice(product.getBasePrice())
                .moq(product.getMoq())
                .stockQuantity(product.getStockQuantity())
                .unit(product.getUnit())
                .status(product.getStatus())
                .isApproved(product.getIsApproved())
                .primaryImageUrl(product.getPrimaryImageUrl())
                .imageUrls(product.getImageUrls())
                .specifications(product.getSpecifications())
                .priceTiers(tiersResponse)
                .tags(product.getTags())
                .viewCount(product.getViewCount())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
