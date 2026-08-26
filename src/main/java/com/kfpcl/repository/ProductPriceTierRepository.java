package com.kfpcl.repository;

import com.kfpcl.entity.ProductPriceTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductPriceTierRepository extends JpaRepository<ProductPriceTier, Long> {

    List<ProductPriceTier> findByProductIdOrderByMinQuantityAsc(Long productId);

    List<ProductPriceTier> findByProductIdInOrderByMinQuantityAsc(List<Long> productIds);

    void deleteByProductId(Long productId);
}
