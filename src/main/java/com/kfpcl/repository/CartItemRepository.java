package com.kfpcl.repository;

import com.kfpcl.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, String> {
    List<CartItem> findByCartId(String cartId);
    Optional<CartItem> findByCartIdAndProductIdAndVariantId(String cartId, String productId, String variantId);
}
