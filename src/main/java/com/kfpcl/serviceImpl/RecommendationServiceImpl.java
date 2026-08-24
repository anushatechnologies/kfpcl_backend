package com.kfpcl.serviceImpl;

import com.kfpcl.dto.ProductResponse;
import com.kfpcl.dto.SupplierSummaryDto;
import com.kfpcl.entity.Buyer;
import com.kfpcl.entity.Product;
import com.kfpcl.repository.ProductRepository;
import com.kfpcl.repository.RecentlyViewedRepository;
import com.kfpcl.repository.WishlistRepository;
import com.kfpcl.util.SecurityUtils;
import com.kfpcl.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final RecentlyViewedRepository recentlyViewedRepository;
    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getBuyerRecommendations() {
        Buyer buyer = securityUtils.getCurrentBuyer();

        Set<String> categoryIds = new HashSet<>(recentlyViewedRepository.findDistinctCategoryIdsByBuyerId(buyer.getId()));
        wishlistRepository.findByBuyerIdOrderByCreatedAtDesc(buyer.getId())
                .forEach(w -> {
                    if (w.getProduct() != null && w.getProduct().getCategory() != null) {
                        categoryIds.add(w.getProduct().getCategory().getId());
                    }
                });

        Map<String, Product> recommendedMap = new LinkedHashMap<>();

        // Recommend from preferred categories
        for (String catId : categoryIds) {
            List<Product> catProducts = productRepository.findByCategoryIdAndStatus(catId, Product.Status.ACTIVE);
            for (Product p : catProducts) {
                recommendedMap.put(p.getId(), p);
            }
        }

        // Also add featured products if list is short
        if (recommendedMap.size() < 10) {
            List<Product> featured = productRepository.findByFeaturedTrueAndStatus(Product.Status.ACTIVE);
            for (Product p : featured) {
                recommendedMap.put(p.getId(), p);
            }
        }

        // If still empty, add latest active products
        if (recommendedMap.isEmpty()) {
            List<Product> latest = productRepository.findTop10ByStatusOrderByCreatedAtDesc(Product.Status.ACTIVE);
            for (Product p : latest) {
                recommendedMap.put(p.getId(), p);
            }
        }

        return recommendedMap.values().stream()
                .limit(10)
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
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
}
