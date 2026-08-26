package com.kfpcl.serviceImpl;

import com.kfpcl.dto.request.ProductCreateRequest;
import com.kfpcl.dto.request.ProductPriceTierRequest;
import com.kfpcl.dto.request.ProductUpdateRequest;
import com.kfpcl.dto.response.PageResponse;
import com.kfpcl.dto.response.ProductPriceTierResponse;
import com.kfpcl.dto.response.ProductResponse;
import com.kfpcl.entity.Category;
import com.kfpcl.entity.Product;
import com.kfpcl.entity.ProductPriceTier;
import com.kfpcl.entity.Seller;
import com.kfpcl.entity.enums.ProductStatus;
import com.kfpcl.exception.DuplicateResourceException;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.exception.UnverifiedSellerException;
import com.kfpcl.repository.CategoryRepository;
import com.kfpcl.repository.ProductRepository;
import com.kfpcl.service.SellerProductService;
import com.kfpcl.service.SellerProfileService;
import com.kfpcl.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerProductServiceImpl implements SellerProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SellerProfileService sellerProfileService;

    @Override
    @Transactional
    public ProductResponse createProduct(String userEmail, ProductCreateRequest request) {
        Seller seller = sellerProfileService.getSellerEntityByEmail(userEmail);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        if (Boolean.FALSE.equals(category.getIsActive())) {
            throw new IllegalArgumentException("Selected category is currently inactive");
        }

        // Validate Status against Seller Verification Status
        ProductStatus targetStatus = request.getStatus() != null ? request.getStatus() : ProductStatus.DRAFT;
        boolean isApproved = false;

        if (targetStatus == ProductStatus.ACTIVE) {
            if (!seller.isAllowedToPublish()) {
                throw new UnverifiedSellerException(
                        "Unverified sellers cannot publish live active products. Please submit as DRAFT or complete seller verification."
                );
            }
            isApproved = true;
        }

        // Validate SKU uniqueness for this seller
        if (request.getSku() != null && !request.getSku().isBlank()) {
            if (productRepository.existsBySkuAndSellerId(request.getSku().trim(), seller.getId())) {
                throw new DuplicateResourceException("Product", "SKU", request.getSku().trim());
            }
        }

        // Generate Slug
        String baseSlug = (request.getSlug() != null && !request.getSlug().isBlank())
                ? SlugUtil.toSlug(request.getSlug())
                : SlugUtil.toSlug(request.getName());

        String slug = baseSlug;
        if (productRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + UUID.randomUUID().toString().substring(0, 6);
        }

        Product product = Product.builder()
                .seller(seller)
                .category(category)
                .name(request.getName().trim())
                .slug(slug)
                .sku(request.getSku() != null ? request.getSku().trim() : null)
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .moq(request.getMoq())
                .stockQuantity(request.getStockQuantity())
                .unit(request.getUnit().trim().toUpperCase())
                .status(targetStatus)
                .isApproved(isApproved)
                .primaryImageUrl(request.getPrimaryImageUrl())
                .imageUrls(request.getImageUrls() != null ? request.getImageUrls() : new ArrayList<>())
                .specifications(request.getSpecifications() != null ? request.getSpecifications() : new java.util.HashMap<>())
                .tags(request.getTags())
                .priceTiers(new ArrayList<>())
                .build();

        // Process Price Tiers
        if (request.getPriceTiers() != null && !request.getPriceTiers().isEmpty()) {
            validateAndSetPriceTiers(product, request.getPriceTiers(), request.getMoq());
        }

        Product savedProduct = productRepository.save(product);
        return mapToResponse(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getSellerProducts(
            String userEmail,
            ProductStatus status,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        Seller seller = sellerProfileService.getSellerEntityByEmail(userEmail);

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> productPage;
        if (status != null) {
            productPage = productRepository.findBySellerIdAndStatus(seller.getId(), status, pageable);
        } else {
            productPage = productRepository.findBySellerId(seller.getId(), pageable);
        }

        List<ProductResponse> mapped = productPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.from(productPage, mapped);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getSellerProductById(String userEmail, Long productId) {
        Seller seller = sellerProfileService.getSellerEntityByEmail(userEmail);
        Product product = productRepository.findByIdAndSellerId(productId, seller.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        return mapToResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(String userEmail, Long productId, ProductUpdateRequest request) {
        Seller seller = sellerProfileService.getSellerEntityByEmail(userEmail);

        Product product = productRepository.findByIdAndSellerId(productId, seller.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        // Validate SKU uniqueness
        if (request.getSku() != null && !request.getSku().isBlank()) {
            if (productRepository.existsBySkuAndSellerIdAndIdNot(request.getSku().trim(), seller.getId(), productId)) {
                throw new DuplicateResourceException("Product", "SKU", request.getSku().trim());
            }
            product.setSku(request.getSku().trim());
        }

        // Validate Status Change
        if (request.getStatus() != null) {
            if (request.getStatus() == ProductStatus.ACTIVE && !seller.isAllowedToPublish()) {
                throw new UnverifiedSellerException(
                        "Cannot activate product. Unverified sellers cannot publish live products to the catalog."
                );
            }
            product.setStatus(request.getStatus());
            if (request.getStatus() == ProductStatus.ACTIVE && seller.isAllowedToPublish()) {
                product.setIsApproved(true);
            }
        }

        product.setCategory(category);
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription());
        product.setBasePrice(request.getBasePrice());
        product.setMoq(request.getMoq());
        product.setStockQuantity(request.getStockQuantity());
        product.setUnit(request.getUnit().trim().toUpperCase());
        product.setPrimaryImageUrl(request.getPrimaryImageUrl());

        if (request.getImageUrls() != null) {
            product.setImageUrls(request.getImageUrls());
        }
        if (request.getSpecifications() != null) {
            product.setSpecifications(request.getSpecifications());
        }
        if (request.getTags() != null) {
            product.setTags(request.getTags());
        }

        // Re-sync Price Tiers
        product.getPriceTiers().clear();
        if (request.getPriceTiers() != null && !request.getPriceTiers().isEmpty()) {
            validateAndSetPriceTiers(product, request.getPriceTiers(), request.getMoq());
        }

        Product updatedProduct = productRepository.save(product);
        return mapToResponse(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(String userEmail, Long productId) {
        Seller seller = sellerProfileService.getSellerEntityByEmail(userEmail);
        Product product = productRepository.findByIdAndSellerId(productId, seller.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        // Soft delete / archive product
        product.setStatus(ProductStatus.ARCHIVED);
        productRepository.save(product);
    }

    private void validateAndSetPriceTiers(Product product, List<ProductPriceTierRequest> tierRequests, int moq) {
        // Sort requests by minQuantity ascending
        List<ProductPriceTierRequest> sorted = tierRequests.stream()
                .sorted((a, b) -> Integer.compare(a.getMinQuantity(), b.getMinQuantity()))
                .collect(Collectors.toList());

        for (int i = 0; i < sorted.size(); i++) {
            ProductPriceTierRequest tierReq = sorted.get(i);

            if (tierReq.getMinQuantity() < moq && i == 0) {
                throw new IllegalArgumentException("First price tier minQuantity must be >= product MOQ (" + moq + ")");
            }

            if (tierReq.getMaxQuantity() != null && tierReq.getMaxQuantity() < tierReq.getMinQuantity()) {
                throw new IllegalArgumentException("Tier maxQuantity (" + tierReq.getMaxQuantity() +
                        ") cannot be less than minQuantity (" + tierReq.getMinQuantity() + ")");
            }

            ProductPriceTier tier = ProductPriceTier.builder()
                    .product(product)
                    .minQuantity(tierReq.getMinQuantity())
                    .maxQuantity(tierReq.getMaxQuantity())
                    .price(tierReq.getPrice())
                    .build();

            product.getPriceTiers().add(tier);
        }
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
