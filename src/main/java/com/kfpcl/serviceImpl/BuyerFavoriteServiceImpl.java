package com.kfpcl.serviceImpl;

import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.dto.response.FavoriteResponse;
import com.kfpcl.entity.Favorite;
import com.kfpcl.entity.Product;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.FavoriteRepository;
import com.kfpcl.repository.ProductRepository;
import com.kfpcl.service.BuyerFavoriteService;
import com.kfpcl.util.ImageUtils;
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
public class BuyerFavoriteServiceImpl implements BuyerFavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;
    private final ImageUtils imageUtils;

    @Override
    @Transactional
    public void toggleFavorite(String buyerId, String productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found");
        }

        if (favoriteRepository.existsByBuyerIdAndProductId(buyerId, productId)) {
            favoriteRepository.deleteByBuyerIdAndProductId(buyerId, productId);
        } else {
            Favorite favorite = Favorite.builder()
                    .id(UUID.randomUUID().toString())
                    .buyerId(buyerId)
                    .productId(productId)
                    .build();
            favoriteRepository.save(favorite);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<FavoriteResponse> getFavorites(String buyerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Favorite> favorites = favoriteRepository.findByBuyerId(buyerId, pageable);

        List<FavoriteResponse> content = favorites.getContent().stream()
                .map(fav -> {
                    Product product = productRepository.findById(fav.getProductId()).orElse(null);
                    return FavoriteResponse.builder()
                            .id(fav.getId())
                            .productId(fav.getProductId())
                            .productName(product != null ? product.getProductName() : "Unknown Product")
                            .price(product != null ? product.getPrice() : 0.0)
                            .imageUrl(product != null ? imageUtils.generatePresignedUrl(product.getImageUrl()) : null)
                            .savedAt(fav.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        return PageResponseDto.<FavoriteResponse>builder()
                .content(content)
                .page(favorites.getNumber())
                .size(favorites.getSize())
                .totalElements(favorites.getTotalElements())
                .totalPages(favorites.getTotalPages())
                .isLast(favorites.isLast())
                .build();
    }
}
