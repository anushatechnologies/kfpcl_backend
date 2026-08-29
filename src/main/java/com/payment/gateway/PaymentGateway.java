package com.payment.gateway;

import com.payment.entity.enums.PaymentGatewayType;

public interface PaymentGateway {

    PaymentGatewayType getGatewayType();

    GatewayOrderResponse createOrder(GatewayOrderRequest request);

    boolean verifySignature(String orderId, String paymentId, String signature);

    GatewayRefundResponse processRefund(GatewayRefundRequest request);
}
