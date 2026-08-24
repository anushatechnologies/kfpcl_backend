package com.kfpcl.repository;

import com.kfpcl.entity.InventoryLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InventoryLogRepository extends JpaRepository<InventoryLog, String> {

    List<InventoryLog> findByInventoryIdOrderByCreatedAtDesc(String inventoryId);

    Page<InventoryLog> findByInventoryIdOrderByCreatedAtDesc(String inventoryId, Pageable pageable);

    List<InventoryLog> findByProductIdOrderByCreatedAtDesc(String productId);
}
