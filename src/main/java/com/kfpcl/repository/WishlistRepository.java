package com.kfpcl.repository;

import com.kfpcl.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, String> {
    List<Wishlist> findByBuyerIdOrderByCreatedAtDesc(String buyerId);
    Optional<Wishlist> findByBuyerIdAndProductId(String buyerId, String productId);
    boolean existsByBuyerIdAndProductId(String buyerId, String productId);
    void deleteByBuyerIdAndProductId(String buyerId, String productId);
    long countByBuyerId(String buyerId);
}
