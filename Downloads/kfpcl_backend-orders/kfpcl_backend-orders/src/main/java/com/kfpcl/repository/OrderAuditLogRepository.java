package com.kfpcl.repository;

import com.kfpcl.entity.OrderAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderAuditLogRepository extends JpaRepository<OrderAuditLog, Long> {
    List<OrderAuditLog> findByOrderIdOrderByTimestampAsc(Long orderId);
}
