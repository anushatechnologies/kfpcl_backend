package com.kfpcl.repository;

import com.kfpcl.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String>, JpaSpecificationExecutor<Product> {
    List<Product> findByFeaturedTrueAndStatus(Product.Status status);
    List<Product> findByCategoryIdAndStatus(String categoryId, Product.Status status);
    List<Product> findTop10ByStatusOrderByCreatedAtDesc(Product.Status status);
}
