package com.kfpcl.repository;

import com.kfpcl.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByBuyerIdAndProductId(Long buyerId, Long productId);

    boolean existsByBuyerIdAndProductId(Long buyerId, Long productId);

    void deleteByBuyerIdAndProductId(Long buyerId, Long productId);

    Page<Favorite> findByBuyerId(Long buyerId, Pageable pageable);

    long countByBuyerId(Long buyerId);
}
