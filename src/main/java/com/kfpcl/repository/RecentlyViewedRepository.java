package com.kfpcl.repository;

import com.kfpcl.entity.RecentlyViewed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecentlyViewedRepository extends JpaRepository<RecentlyViewed, String> {
    List<RecentlyViewed> findByBuyerIdOrderByViewedAtDesc(String buyerId);
    Optional<RecentlyViewed> findByBuyerIdAndProductId(String buyerId, String productId);

    @Query("SELECT DISTINCT rv.product.category.id FROM RecentlyViewed rv WHERE rv.buyer.id = :buyerId")
    List<String> findDistinctCategoryIdsByBuyerId(@Param("buyerId") String buyerId);
}
