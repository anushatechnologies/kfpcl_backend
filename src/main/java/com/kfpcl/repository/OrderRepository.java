package com.kfpcl.repository;

import com.kfpcl.entity.Order;
import com.kfpcl.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    List<Order> findByBuyerId(String buyerId);

    List<Order> findBySellerId(String sellerId);

    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    List<Order> findByBuyerIdAndStatus(String buyerId, OrderStatus status);

    List<Order> findBySellerIdAndStatus(String sellerId, OrderStatus status);

    @Query("SELECT COUNT(DISTINCT o.buyerId) FROM Order o WHERE o.sellerId = :sellerId GROUP BY o.buyerId HAVING COUNT(o.id) > 1")
    List<Long> countRepeatBuyersBySellerId(@Param("sellerId") String sellerId);

    @Query("SELECT SUM(o.grandTotal) FROM Order o")
    Double sumTotalRevenue();

    long countByStatus(OrderStatus status);

    @Query("SELECT 'General Region', COUNT(o.id), SUM(o.grandTotal) FROM Order o")
    List<Object[]> findRegionStatistics();
}
