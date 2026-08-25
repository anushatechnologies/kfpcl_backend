package com.kfpcl.repository;

import com.kfpcl.entity.OrderTracking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderTrackingRepository extends JpaRepository<OrderTracking, String> {

    List<OrderTracking> findByOrderIdOrderByCreatedAtDesc(String orderId);
}
