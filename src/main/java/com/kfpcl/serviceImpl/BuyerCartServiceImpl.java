package com.kfpcl.serviceImpl;

import com.kfpcl.dto.CartDto;
import com.kfpcl.dto.CartItemDto;
import com.kfpcl.dto.request.CartItemUpsertRequest;
import com.kfpcl.entity.Cart;
import com.kfpcl.entity.CartItem;
import com.kfpcl.entity.Product;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.CartItemRepository;
import com.kfpcl.repository.CartRepository;
import com.kfpcl.repository.ProductRepository;
import com.kfpcl.service.BuyerCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BuyerCartServiceImpl implements BuyerCartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public CartDto getCart(String buyerId) {
        Cart cart = getOrCreateCart(buyerId);
        return mapCartToDto(cart);
    }

    @Override
    @Transactional
    public CartDto upsertCartItem(String buyerId, CartItemUpsertRequest request) {
        Cart cart = getOrCreateCart(buyerId);

        Optional<CartItem> existingItemOpt = cartItemRepository
                .findByCartIdAndProductIdAndVariantId(cart.getId(), request.getProductId(), request.getVariantId());

        if (existingItemOpt.isPresent()) {
            CartItem item = existingItemOpt.get();
            item.setQuantity(request.getQuantity());
            cartItemRepository.save(item);
        } else {
            CartItem newItem = CartItem.builder()
                    .id(UUID.randomUUID().toString())
                    .cartId(cart.getId())
                    .productId(request.getProductId())
                    .variantId(request.getVariantId())
                    .quantity(request.getQuantity())
                    .build();
            cartItemRepository.save(newItem);
        }

        return mapCartToDto(cart);
    }

    @Override
    @Transactional
    public CartDto removeCartItem(String buyerId, String itemId) {
        Cart cart = getOrCreateCart(buyerId);
        
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
                
        if (!item.getCartId().equals(cart.getId())) {
            throw new IllegalArgumentException("Item does not belong to this cart");
        }
        
        cartItemRepository.delete(item);
        return mapCartToDto(cart);
    }

    private Cart getOrCreateCart(String buyerId) {
        return cartRepository.findByBuyerId(buyerId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .id(UUID.randomUUID().toString())
                            .buyerId(buyerId)
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    private CartDto mapCartToDto(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        List<CartItemDto> itemDtos = items.stream().map(item -> {
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            return CartItemDto.builder()
                    .id(item.getId())
                    .productId(item.getProductId())
                    .productName(product != null ? product.getProductName() : "Unknown Product")
                    .variantId(item.getVariantId())
                    .quantity(item.getQuantity())
                    .createdAt(item.getCreatedAt())
                    .updatedAt(item.getUpdatedAt())
                    .build();
        }).collect(Collectors.toList());

        return CartDto.builder()
                .id(cart.getId())
                .buyerId(cart.getBuyerId())
                .items(itemDtos)
                .build();
    }
}
