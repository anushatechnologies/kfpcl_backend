package com.kfpcl.serviceImpl;

import com.kfpcl.dto.ProductResponse;
import com.kfpcl.dto.SupplierSummaryDto;
import com.kfpcl.entity.Buyer;
import com.kfpcl.entity.Product;
import com.kfpcl.entity.RecentlyViewed;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.ProductRepository;
import com.kfpcl.repository.RecentlyViewedRepository;
import com.kfpcl.util.SecurityUtils;
import com.kfpcl.service.RecentlyViewedService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecentlyViewedServiceImpl implements RecentlyViewedService {

    private final RecentlyViewedRepository recentlyViewedRepository;
    private final ProductRepository productRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public void recordProductView(String productId) {
        Buyer buyer = securityUtils.getCurrentBuyer();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        Optional<RecentlyViewed> existing = recentlyViewedRepository.findByBuyerIdAndProductId(buyer.getId(), productId);

        if (existing.isPresent()) {
            RecentlyViewed viewed = existing.get();
            viewed.setViewedAt(LocalDateTime.now());
            recentlyViewedRepository.save(viewed);
        } else {
            RecentlyViewed viewed = RecentlyViewed.builder()
                    .id(UUID.randomUUID().toString())
                    .buyer(buyer)
                    .product(product)
                    .viewedAt(LocalDateTime.now())
                    .build();
            recentlyViewedRepository.save(viewed);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getRecentlyViewed() {
        Buyer buyer = securityUtils.getCurrentBuyer();
        return recentlyViewedRepository.findByBuyerIdOrderByViewedAtDesc(buyer.getId())
                .stream()
                .map(rv -> mapToProductResponse(rv.getProduct()))
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
