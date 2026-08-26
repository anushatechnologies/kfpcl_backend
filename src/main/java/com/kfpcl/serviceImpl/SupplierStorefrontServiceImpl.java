package com.kfpcl.serviceImpl;

import com.kfpcl.dto.response.PageResponse;
import com.kfpcl.dto.response.ProductPriceTierResponse;
import com.kfpcl.dto.response.ProductResponse;
import com.kfpcl.dto.response.SupplierStorefrontResponse;
import com.kfpcl.entity.Product;
import com.kfpcl.entity.Seller;
import com.kfpcl.entity.enums.ProductStatus;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.ProductRepository;
import com.kfpcl.repository.SellerRepository;
import com.kfpcl.service.SupplierStorefrontService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierStorefrontServiceImpl implements SupplierStorefrontService {

    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public SupplierStorefrontResponse getStorefrontById(Long supplierId) {
        Seller seller = sellerRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", supplierId));

        // Get count of active and approved products
        long activeProductsCount = productRepository.countBySellerIdAndStatusAndIsApprovedTrue(
                seller.getId(), ProductStatus.ACTIVE
        );

        // Fetch top 6 featured active products
        Pageable topSix = PageRequest.of(0, 6, Sort.by("viewCount").descending().and(Sort.by("createdAt").descending()));
        Page<Product> featuredPage = productRepository.findBySellerIdAndStatusAndIsApprovedTrue(
                seller.getId(), ProductStatus.ACTIVE, topSix
        );

        List<ProductResponse> featuredProducts = featuredPage.getContent().stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());

        return SupplierStorefrontResponse.builder()
                .id(seller.getId())
                .companyName(seller.getCompanyName())
                .businessRegistrationNumber(seller.getBusinessRegistrationNumber())
                .taxId(seller.getTaxId())
                .description(seller.getDescription())
                .logoUrl(seller.getLogoUrl())
                .bannerUrl(seller.getBannerUrl())
                .city(seller.getCity())
                .state(seller.getState())
                .country(seller.getCountry())
                .postalCode(seller.getPostalCode())
                .yearEstablished(seller.getYearEstablished())
                .rating(seller.getRating())
                .totalReviews(seller.getTotalReviews())
                .isVerified(seller.getIsVerified())
                .verificationStatus(seller.getVerificationStatus())
                .activeProductsCount(activeProductsCount)
                .featuredProducts(featuredProducts)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getSupplierProducts(
            Long supplierId,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        if (!sellerRepository.existsById(supplierId)) {
            throw new ResourceNotFoundException("Supplier", "id", supplierId);
        }

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage = productRepository.findBySellerIdAndStatusAndIsApprovedTrue(
                supplierId, ProductStatus.ACTIVE, pageable
        );

        List<ProductResponse> mapped = productPage.getContent().stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());

        return PageResponse.from(productPage, mapped);
    }

    private ProductResponse mapToProductResponse(Product product) {
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
