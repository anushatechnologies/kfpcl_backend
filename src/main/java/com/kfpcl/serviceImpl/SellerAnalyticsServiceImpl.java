package com.kfpcl.serviceImpl;

import com.kfpcl.dto.SellerAnalyticsDto;
import com.kfpcl.entity.Order;
import com.kfpcl.entity.OrderStatus;
import com.kfpcl.repository.OrderRepository;
import com.kfpcl.service.SellerAnalyticsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class SellerAnalyticsServiceImpl implements SellerAnalyticsService {

    private final OrderRepository orderRepository;

    public SellerAnalyticsServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public SellerAnalyticsDto getSellerAnalytics(String sellerId) {
        List<Order> sellerOrders = orderRepository.findBySellerId(sellerId);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        long completedOrders = 0;
        long pendingOrders = 0;

        for (Order order : sellerOrders) {
            if (order.getStatus() == OrderStatus.DELIVERED) {
                totalRevenue = totalRevenue.add(order.getGrandTotal());
                completedOrders++;
            } else if (order.getStatus() != OrderStatus.CANCELLED) {
                pendingOrders++;
            }
        }

        long totalOrders = sellerOrders.size();
        BigDecimal averageOrderValue = BigDecimal.ZERO;
        if (totalOrders > 0) {
            BigDecimal grandTotalSum = sellerOrders.stream()
                    .map(Order::getGrandTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            averageOrderValue = grandTotalSum.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);
        }

        List<Long> repeatBuyerCounts = orderRepository.countRepeatBuyersBySellerId(sellerId);
        long repeatBuyersCount = repeatBuyerCounts != null ? repeatBuyerCounts.size() : 0;

        return SellerAnalyticsDto.builder()
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .completedOrders(completedOrders)
                .pendingOrders(pendingOrders)
                .averageOrderValue(averageOrderValue)
                .repeatBuyersCount(repeatBuyersCount)
                .build();
    }
}
