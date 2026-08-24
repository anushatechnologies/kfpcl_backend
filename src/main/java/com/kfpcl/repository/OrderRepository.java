package com.kfpcl.repository;

import com.kfpcl.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByBuyerIdOrderByCreatedAtDesc(String buyerId);
    List<Order> findByBuyerIdAndStatusOrderByCreatedAtDesc(String buyerId, Order.Status status);
    Optional<Order> findByIdAndBuyerId(String id, String buyerId);
    long countByBuyerId(String buyerId);
    List<Order> findTop5ByBuyerIdOrderByCreatedAtDesc(String buyerId);
}
