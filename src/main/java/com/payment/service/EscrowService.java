package com.payment.service;

import com.payment.entity.EscrowAccount;

import java.math.BigDecimal;
import java.util.Optional;

public interface EscrowService {

    EscrowAccount lockEscrow(String orderId, Long transactionId, BigDecimal amount, String virtualAccountNumber, String ifscCode);

    EscrowAccount releaseFundsOnDelivery(String orderId, String clientIp);

    EscrowAccount refundEscrow(String orderId, String clientIp);

    Optional<EscrowAccount> getEscrowByOrderId(String orderId);
}
