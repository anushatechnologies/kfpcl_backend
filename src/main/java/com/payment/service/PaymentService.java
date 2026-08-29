package com.payment.service;

import com.payment.entity.PaymentTransaction;
import com.payment.entity.enums.PaymentMethod;
import com.payment.entity.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.Optional;

public interface PaymentService {

    PaymentTransaction createTransaction(String orderId, String buyerId, String sellerId,
                                        PaymentMethod paymentMethod, BigDecimal amount, String currency);

    Optional<PaymentTransaction> getTransactionByOrderId(String orderId);

    PaymentTransaction updatePaymentStatus(String orderId, PaymentStatus nextStatus, String reason, String clientIp);
}
