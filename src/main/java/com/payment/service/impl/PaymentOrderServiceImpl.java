package com.payment.service.impl;

import com.payment.dto.gateway.CreateGatewayOrderRequest;
import com.payment.dto.gateway.CreateGatewayOrderResponse;
import com.payment.entity.PaymentTransaction;
import com.payment.entity.enums.PaymentGatewayType;
import com.payment.entity.enums.PaymentMethod;
import com.payment.entity.enums.PaymentStatus;
import com.payment.exception.BadRequestException;
import com.payment.gateway.GatewayOrderRequest;
import com.payment.gateway.GatewayOrderResponse;
import com.payment.gateway.PaymentGateway;
import com.payment.gateway.PaymentGatewayFactory;
import com.payment.integration.order.OrderDetailsDto;
import com.payment.integration.order.OrderPaymentClient;
import com.payment.repository.PaymentTransactionRepository;
import com.payment.security.SecurityUtils;
import com.payment.security.UserContext;
import com.payment.service.PaymentOrderService;
import com.payment.service.audit.PaymentAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentOrderServiceImpl implements PaymentOrderService {

    private final PaymentTransactionRepository transactionRepository;
    private final PaymentGatewayFactory gatewayFactory;
    private final OrderPaymentClient orderPaymentClient;
    private final SecurityUtils securityUtils;
    private final PaymentAuditService auditService;

    @org.springframework.beans.factory.annotation.Value("${razorpay.key-id:rzp_live_TO6q7NUVnPM6bA}")
    private String razorpayKeyId;

    @Override
    @Transactional
    public CreateGatewayOrderResponse createGatewayOrder(CreateGatewayOrderRequest request, String idempotencyKey, String clientIp) {
        UserContext currentUser = securityUtils.getCurrentUser();
        log.info("Creating gateway order for orderId: {}, gateway: {}, user: {}",
                request.getOrderId(), request.getGateway(), currentUser.getUserId());

        // 1. Check Idempotency Key if provided
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<PaymentTransaction> existingTxn = transactionRepository.findByIdempotencyKey(idempotencyKey);
            if (existingTxn.isPresent()) {
                PaymentTransaction txn = existingTxn.get();
                log.info("Idempotent hit for key: {}. Returning existing transaction: {}", idempotencyKey, txn.getTransactionReference());
                return mapToResponse(txn);
            }
        }

        // 2. Fetch trusted Order Details from Order Module (never trust client amount)
        OrderDetailsDto order = orderPaymentClient.getOrderDetails(request.getOrderId());
        if (order == null) {
            throw new BadRequestException("Order not found with ID: " + request.getOrderId());
        }

        // 3. Verify that the authenticated buyer owns the order
        securityUtils.verifyBuyerOwnership(order.getBuyerId());

        // 4. Generate local transaction reference
        String txnRef = "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();

        // 5. Call Payment Gateway
        PaymentGatewayType gatewayType = request.getGateway() != null ? request.getGateway() : PaymentGatewayType.RAZORPAY;
        PaymentGateway gateway = gatewayFactory.getGateway(gatewayType);
        GatewayOrderRequest gatewayReq = GatewayOrderRequest.builder()
                .orderId(order.getOrderId())
                .amount(order.getGrandTotal())
                .currency(order.getCurrency())
                .receipt(txnRef)
                .build();

        GatewayOrderResponse gatewayResp = gateway.createOrder(gatewayReq);

        // 6. Save Payment Transaction with PENDING_PAYMENT
        PaymentMethod method = request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.CARD;
        PaymentTransaction transaction = PaymentTransaction.builder()
                .transactionReference(txnRef)
                .orderId(order.getOrderId())
                .buyerId(order.getBuyerId())
                .sellerId(order.getSellerId())
                .paymentMethod(method)
                .gateway(gatewayType)
                .amount(order.getGrandTotal())
                .currency(order.getCurrency())
                .status(PaymentStatus.PENDING_PAYMENT)
                .gatewayOrderId(gatewayResp.getGatewayOrderId())
                .idempotencyKey(idempotencyKey)
                .recipientEmail(order.getCustomerEmail())
                .build();

        PaymentTransaction savedTxn = transactionRepository.save(transaction);

        // 7. Audit Log
        auditService.logAction(
                savedTxn.getId(),
                order.getOrderId(),
                "CREATE_GATEWAY_ORDER",
                null,
                PaymentStatus.PENDING_PAYMENT.name(),
                currentUser.getUserId(),
                gatewayType.name(),
                clientIp,
                "Gateway Order Created: " + gatewayResp.getGatewayOrderId()
        );

        return mapToResponse(savedTxn);
    }

    private CreateGatewayOrderResponse mapToResponse(PaymentTransaction txn) {
        return CreateGatewayOrderResponse.builder()
                .transactionReference(txn.getTransactionReference())
                .orderId(txn.getOrderId())
                .gatewayOrderId(txn.getGatewayOrderId())
                .gateway(txn.getGateway())
                .amount(txn.getAmount())
                .currency(txn.getCurrency())
                .status(txn.getStatus())
                .keyId(razorpayKeyId)
                .idempotencyKey(txn.getIdempotencyKey())
                .createdAt(txn.getCreatedAt())
                .build();
    }
}
