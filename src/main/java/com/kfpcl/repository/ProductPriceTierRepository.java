package com.kfpcl.repository;

import com.kfpcl.entity.ProductPriceTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductPriceTierRepository extends JpaRepository<ProductPriceTier, String> {
    List<ProductPriceTier> findByProductIdOrderByMinQuantityAsc(String productId);
    void deleteByProductId(String productId);
}
