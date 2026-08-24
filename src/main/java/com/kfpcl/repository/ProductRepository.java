package com.kfpcl.repository;

import com.kfpcl.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String>, JpaSpecificationExecutor<Product> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, String id);

    List<Product> findByCategoryId(String categoryId);

    List<Product> findBySubcategoryId(String subcategoryId);

    Page<Product> findByStatus(Product.Status status, Pageable pageable);
}
