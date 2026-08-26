package com.kfpcl.service;

import com.kfpcl.dto.OrderCreateDto;
import com.kfpcl.dto.OrderResponseDto;
import com.kfpcl.dto.OrderStatusUpdateDto;

import java.util.List;

public interface OrderService {

    OrderResponseDto createOrder(OrderCreateDto orderCreateDto, String buyerId, String idempotencyKey);

    List<OrderResponseDto> getBuyerOrders(String buyerId);

    OrderResponseDto getBuyerOrderById(Long orderId, String buyerId);

    List<OrderResponseDto> getSellerOrders(String sellerId);

    OrderResponseDto updateOrderStatus(Long orderId, OrderStatusUpdateDto updateDto, String sellerId);
}
