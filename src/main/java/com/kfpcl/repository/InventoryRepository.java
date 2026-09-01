package com.kfpcl.repository;

import com.kfpcl.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, String> {

    Optional<Inventory> findByProductId(String productId);

    Optional<Inventory> findBySku(String sku);

    boolean existsByProductId(String productId);

    @Query("SELECT i FROM Inventory i JOIN Product p ON i.productId = p.id WHERE p.sellerId = :sellerId")
    Page<Inventory> findBySellerId(@Param("sellerId") String sellerId, Pageable pageable);

    Page<Inventory> findByStatus(Inventory.Status status, Pageable pageable);

    Page<Inventory> findBySkuContainingIgnoreCase(String sku, Pageable pageable);

    void deleteByProductId(String productId);
}
