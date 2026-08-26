package com.kfpcl.repository;

import com.kfpcl.entity.Product;
import com.kfpcl.entity.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Page<Product> findBySellerId(Long sellerId, Pageable pageable);

    Page<Product> findBySellerIdAndStatus(Long sellerId, ProductStatus status, Pageable pageable);

    Page<Product> findBySellerIdAndStatusAndIsApprovedTrue(Long sellerId, ProductStatus status, Pageable pageable);

    Optional<Product> findByIdAndSellerId(Long id, Long sellerId);

    Optional<Product> findBySlug(String slug);

    Optional<Product> findByIdAndStatusAndIsApprovedTrue(Long id, ProductStatus status);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    boolean existsBySkuAndSellerId(String sku, Long sellerId);

    boolean existsBySkuAndSellerIdAndIdNot(String sku, Long sellerId, Long id);

    long countBySellerId(Long sellerId);

    long countBySellerIdAndStatus(Long sellerId, ProductStatus status);

    long countBySellerIdAndStatusAndIsApprovedTrue(Long sellerId, ProductStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(p.viewCount), 0) FROM Product p WHERE p.seller.id = :sellerId")
    Long sumViewCountBySellerId(@org.springframework.data.repository.query.Param("sellerId") Long sellerId);
}
