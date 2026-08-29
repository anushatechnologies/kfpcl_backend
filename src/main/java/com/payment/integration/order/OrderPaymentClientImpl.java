package com.payment.integration.order;

import com.payment.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
public class OrderPaymentClientImpl implements OrderPaymentClient {

    private final ConcurrentMap<String, OrderDetailsDto> orderStore = new ConcurrentHashMap<>();

    public OrderPaymentClientImpl() {
        // 1 Rupee live testing order
        orderStore.put("ORD-1RUPEE", OrderDetailsDto.builder()
                .orderId("ORD-1RUPEE")
                .buyerId("BUYER-101")
                .sellerId("SELLER-501")
                .grandTotal(new BigDecimal("1.00"))
                .currency("INR")
                .orderStatus("CREATED")
                .paymentStatus("UNPAID")
                .customerEmail("buyer101@kfpcl.com")
                .build());

        orderStore.put("ORD-1001", OrderDetailsDto.builder()
                .orderId("ORD-1001")
                .buyerId("BUYER-101")
                .sellerId("SELLER-501")
                .grandTotal(new BigDecimal("50000.00"))
                .currency("INR")
                .orderStatus("CREATED")
                .paymentStatus("UNPAID")
                .customerEmail("buyer101@kfpcl.com")
                .build());

        orderStore.put("ORD-1002", OrderDetailsDto.builder()
                .orderId("ORD-1002")
                .buyerId("BUYER-102")
                .sellerId("SELLER-502")
                .grandTotal(new BigDecimal("125000.00"))
                .currency("INR")
                .orderStatus("CREATED")
                .paymentStatus("UNPAID")
                .customerEmail("buyer102@kfpcl.com")
                .build());
    }

    @Override
    public OrderDetailsDto getOrderDetails(String orderId) {
        OrderDetailsDto order = orderStore.get(orderId);
        if (order == null) {
            // If not found in mock store, create a realistic fallback order dynamically
            log.info("Order {} not found in pre-populated store, creating dynamic mock order for testing", orderId);
            order = OrderDetailsDto.builder()
                    .orderId(orderId)
                    .buyerId("BUYER-CURRENT")
                    .sellerId("SELLER-501")
                    .grandTotal(new BigDecimal("10000.00"))
                    .currency("INR")
                    .orderStatus("CREATED")
                    .paymentStatus("UNPAID")
                    .customerEmail("buyer@kfpcl.com")
                    .build();
            orderStore.put(orderId, order);
        }
        return order;
    }

    @Override
    public void updateOrderPaymentStatus(String orderId, String paymentStatus) {
        OrderDetailsDto order = orderStore.get(orderId);
        if (order != null) {
            order.setPaymentStatus(paymentStatus);
            log.info("Updated order {} payment status to {}", orderId, paymentStatus);
        }
    }

    @Override
    public void registerMockOrder(OrderDetailsDto orderDetailsDto) {
        orderStore.put(orderDetailsDto.getOrderId(), orderDetailsDto);
    }
}
