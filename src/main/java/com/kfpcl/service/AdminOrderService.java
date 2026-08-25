package com.kfpcl.service;

import com.kfpcl.dto.OrderResponseDto;
import com.kfpcl.dto.OrderStatusUpdateDto;
import com.kfpcl.dto.OrderTrackingCreateDto;
import com.kfpcl.dto.OrderTrackingResponseDto;
import com.kfpcl.dto.PageResponseDto;

public interface AdminOrderService {

    PageResponseDto<OrderResponseDto> getOrders(String search, String status, String paymentStatus, String region, int page, int size, String sortBy, String sortDir);

    OrderResponseDto getOrderById(String orderId);

    OrderResponseDto updateOrderStatus(String orderId, OrderStatusUpdateDto dto);

    OrderTrackingResponseDto addOrderTracking(String orderId, OrderTrackingCreateDto dto);

    byte[] exportOrdersCsv();
}
