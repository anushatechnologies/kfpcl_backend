package com.payment.gateway.impl;

import com.payment.entity.enums.PaymentGatewayType;
import com.payment.exception.PaymentGatewayException;
import com.payment.gateway.*;
import org.springframework.stereotype.Component;

/**
 * Stripe Gateway Integration (Stub / Placeholder).
 * Note: Marked as placeholder according to project requirements.
 */
@Component
public class StripeGateway implements PaymentGateway {

    @Override
    public PaymentGatewayType getGatewayType() {
        return PaymentGatewayType.STRIPE;
    }

    @Override
    public GatewayOrderResponse createOrder(GatewayOrderRequest request) {
        throw new PaymentGatewayException("Stripe payment gateway integration is currently in preview/stub mode. Please use RAZORPAY.");
    }

    @Override
    public boolean verifySignature(String orderId, String paymentId, String signature) {
        throw new PaymentGatewayException("Stripe signature verification stub.");
    }

    @Override
    public GatewayRefundResponse processRefund(GatewayRefundRequest request) {
        throw new PaymentGatewayException("Stripe refund stub.");
    }
}
