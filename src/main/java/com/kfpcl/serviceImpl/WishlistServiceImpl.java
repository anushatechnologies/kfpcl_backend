package com.kfpcl.serviceImpl;

import com.kfpcl.dto.ProductResponse;
import com.kfpcl.dto.SupplierSummaryDto;
import com.kfpcl.dto.WishlistItemResponse;
import com.kfpcl.entity.Buyer;
import com.kfpcl.entity.Product;
import com.kfpcl.entity.Wishlist;
import com.kfpcl.exception.ConflictException;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.ProductRepository;
import com.kfpcl.repository.WishlistRepository;
import com.kfpcl.util.SecurityUtils;
import com.kfpcl.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public WishlistItemResponse addToWishlist(String productId) {
        Buyer buyer = securityUtils.getCurrentBuyer();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (wishlistRepository.existsByBuyerIdAndProductId(buyer.getId(), productId)) {
            throw new ConflictException("Product is already in your wishlist");
        }

        Wishlist wishlist = Wishlist.builder()
                .id(UUID.randomUUID().toString())
                .buyer(buyer)
                .product(product)
                .build();

        Wishlist saved = wishlistRepository.save(wishlist);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void removeFromWishlist(String productId) {
        Buyer buyer = securityUtils.getCurrentBuyer();
        Wishlist wishlist = wishlistRepository.findByBuyerIdAndProductId(buyer.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist item not found for product id: " + productId));

        wishlistRepository.delete(wishlist);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WishlistItemResponse> getBuyerWishlist() {
        Buyer buyer = securityUtils.getCurrentBuyer();
        return wishlistRepository.findByBuyerIdOrderByCreatedAtDesc(buyer.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private WishlistItemResponse mapToResponse(Wishlist wishlist) {
        Product p = wishlist.getProduct();
        SupplierSummaryDto supplierDto = null;
        if (p.getSupplier() != null) {
            supplierDto = SupplierSummaryDto.builder()
                    .id(p.getSupplier().getId())
                    .companyName(p.getSupplier().getCompanyName())
                    .gstVerified(p.getSupplier().getGstVerified())
                    .isVerified(p.getSupplier().getIsVerified())
                    .build();
        }

        ProductResponse productResponse = ProductResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .description(p.getDescription())
                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .supplier(supplierDto)
                .price(p.getPrice())
                .unit(p.getUnit())
                .moq(p.getMoq())
                .featured(p.getFeatured())
                .status(p.getStatus().name())
                .imageUrl(p.getImageUrl())
                .build();

        return WishlistItemResponse.builder()
                .id(wishlist.getId())
                .product(productResponse)
                .addedAt(wishlist.getCreatedAt())
                .build();
    }
}
