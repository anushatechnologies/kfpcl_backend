package com.payment.service;

import com.payment.dto.gateway.CreateGatewayOrderRequest;
import com.payment.dto.gateway.CreateGatewayOrderResponse;

public interface PaymentOrderService {

    CreateGatewayOrderResponse createGatewayOrder(CreateGatewayOrderRequest request, String idempotencyKey, String clientIp);
}
