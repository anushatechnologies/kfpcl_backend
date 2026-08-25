package com.kfpcl.repository;

import com.kfpcl.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String>, JpaSpecificationExecutor<Order> {

    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByOrderStatus(Order.OrderStatus orderStatus, Pageable pageable);

    Page<Order> findByBuyerId(String buyerId, Pageable pageable);

    Page<Order> findBySellerId(String sellerId, Pageable pageable);

    long countByOrderStatus(Order.OrderStatus orderStatus);

    @Query("SELECT SUM(o.finalAmount) FROM Order o WHERE o.paymentStatus = 'PAID'")
    Double sumTotalRevenue();

    @Query("SELECT o.region, COUNT(o), SUM(o.finalAmount) FROM Order o GROUP BY o.region")
    List<Object[]> findRegionStatistics();
}
