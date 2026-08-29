package com.kfpcl.repository;

import com.kfpcl.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, String> {
    Page<Favorite> findByBuyerId(String buyerId, Pageable pageable);
    Optional<Favorite> findByBuyerIdAndProductId(String buyerId, String productId);
    boolean existsByBuyerIdAndProductId(String buyerId, String productId);
    void deleteByBuyerIdAndProductId(String buyerId, String productId);
}
