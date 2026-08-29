package com.payment.service.impl;

import com.payment.entity.PaymentTransaction;
import com.payment.entity.enums.PaymentGatewayType;
import com.payment.entity.enums.PaymentMethod;
import com.payment.entity.enums.PaymentStatus;
import com.payment.exception.ResourceNotFoundException;
import com.payment.repository.PaymentTransactionRepository;
import com.payment.security.SecurityUtils;
import com.payment.service.PaymentService;
import com.payment.service.PaymentStateTransitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentTransactionRepository transactionRepository;
    private final PaymentStateTransitionService stateTransitionService;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public PaymentTransaction createTransaction(String orderId, String buyerId, String sellerId,
                                              PaymentMethod paymentMethod, BigDecimal amount, String currency) {
        log.info("Creating initial payment transaction for orderId: {}, buyer: {}, method: {}", orderId, buyerId, paymentMethod);

        String txnRef = "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();

        PaymentTransaction transaction = PaymentTransaction.builder()
                .transactionReference(txnRef)
                .orderId(orderId)
                .buyerId(buyerId)
                .sellerId(sellerId)
                .paymentMethod(paymentMethod)
                .gateway(PaymentGatewayType.NONE)
                .amount(amount)
                .currency(currency != null ? currency : "INR")
                .status(PaymentStatus.PENDING_PAYMENT)
                .build();

        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentTransaction> getTransactionByOrderId(String orderId) {
        return transactionRepository.findByOrderId(orderId);
    }

    @Override
    @Transactional
    public PaymentTransaction updatePaymentStatus(String orderId, PaymentStatus nextStatus, String reason, String clientIp) {
        PaymentTransaction txn = transactionRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found for order: " + orderId));

        String currentUserId = securityUtils.getCurrentUser().getUserId();
        return stateTransitionService.transition(txn, nextStatus, currentUserId, "PAYMENT_SERVICE", clientIp, reason);
    }
}
