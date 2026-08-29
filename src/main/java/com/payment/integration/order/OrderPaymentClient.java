package com.payment.integration.order;

public interface OrderPaymentClient {

    /**
     * Retrieves trusted order details directly from Developer 3's Order module.
     *
     * @param orderId the order ID
     * @return trusted OrderDetailsDto
     */
    OrderDetailsDto getOrderDetails(String orderId);

    /**
     * Updates order payment status in Developer 3's Order module.
     */
    void updateOrderPaymentStatus(String orderId, String paymentStatus);

    /**
     * Registers or mocks an order for testing/development.
     */
    void registerMockOrder(OrderDetailsDto orderDetailsDto);
}
