package com.kfpcl.repository;

import com.kfpcl.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, String> {

    Optional<ProductVariant> findBySku(String sku);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, String id);

    List<ProductVariant> findByProductId(String productId);
}
