package com.kfpcl.serviceImpl;

import com.kfpcl.dto.response.FavoriteResponse;
import com.kfpcl.dto.response.FavoriteToggleResponse;
import com.kfpcl.dto.response.PageResponse;
import com.kfpcl.entity.Buyer;
import com.kfpcl.entity.Favorite;
import com.kfpcl.entity.Product;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.BuyerRepository;
import com.kfpcl.repository.FavoriteRepository;
import com.kfpcl.repository.ProductRepository;
import com.kfpcl.service.BuyerFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BuyerFavoriteServiceImpl implements BuyerFavoriteService {

    private final BuyerRepository buyerRepository;
    private final ProductRepository productRepository;
    private final FavoriteRepository favoriteRepository;

    @Override
    @Transactional
    public FavoriteToggleResponse toggleFavorite(String userEmail, Long productId) {
        Buyer buyer = getBuyerByEmail(userEmail);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        Optional<Favorite> existing = favoriteRepository.findByBuyerIdAndProductId(buyer.getId(), productId);

        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            return FavoriteToggleResponse.builder()
                    .productId(productId)
                    .isFavorited(false)
                    .message("Product removed from favorites")
                    .build();
        } else {
            Favorite favorite = Favorite.builder()
                    .buyer(buyer)
                    .product(product)
                    .build();
            favoriteRepository.save(favorite);
            return FavoriteToggleResponse.builder()
                    .productId(productId)
                    .isFavorited(true)
                    .message("Product added to favorites")
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FavoriteResponse> getBuyerFavorites(String userEmail, int page, int size) {
        Buyer buyer = getBuyerByEmail(userEmail);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Favorite> favoritePage = favoriteRepository.findByBuyerId(buyer.getId(), pageable);

        List<FavoriteResponse> mapped = favoritePage.getContent().stream()
                .map(fav -> {
                    Product prod = fav.getProduct();
                    return FavoriteResponse.builder()
                            .favoriteId(fav.getId())
                            .productId(prod.getId())
                            .productName(prod.getName())
                            .productSlug(prod.getSlug())
                            .basePrice(prod.getBasePrice())
                            .moq(prod.getMoq())
                            .unit(prod.getUnit())
                            .primaryImageUrl(prod.getPrimaryImageUrl())
                            .sellerId(prod.getSeller() != null ? prod.getSeller().getId() : null)
                            .sellerCompanyName(prod.getSeller() != null ? prod.getSeller().getCompanyName() : null)
                            .sellerIsVerified(prod.getSeller() != null ? prod.getSeller().getIsVerified() : false)
                            .savedAt(fav.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        return PageResponse.from(favoritePage, mapped);
    }

    private Buyer getBuyerByEmail(String userEmail) {
        return buyerRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer profile not found for user: " + userEmail));
    }
}
