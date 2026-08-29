package com.payment.service.impl;

import com.payment.dto.escrow.VirtualAccountResponse;
import com.payment.entity.EscrowAccount;
import com.payment.entity.PaymentTransaction;
import com.payment.entity.enums.EscrowReleaseStatus;
import com.payment.entity.enums.PaymentStatus;
import com.payment.exception.BadRequestException;
import com.payment.integration.order.OrderDetailsDto;
import com.payment.integration.order.OrderPaymentClient;
import com.payment.repository.EscrowAccountRepository;
import com.payment.repository.PaymentTransactionRepository;
import com.payment.security.SecurityUtils;
import com.payment.security.UserContext;
import com.payment.service.VirtualAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VirtualAccountServiceImpl implements VirtualAccountService {

    private final EscrowAccountRepository escrowAccountRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final OrderPaymentClient orderPaymentClient;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public VirtualAccountResponse getVirtualAccountDetails(String orderId) {
        UserContext currentUser = securityUtils.getCurrentUser();
        log.info("Fetching virtual account for orderId: {}, user: {}", orderId, currentUser.getUserId());

        // 1. Fetch trusted order details
        OrderDetailsDto order = orderPaymentClient.getOrderDetails(orderId);
        if (order == null) {
            throw new BadRequestException("Order not found with ID: " + orderId);
        }

        // 2. Verify that the order belongs to the authenticated buyer
        securityUtils.verifyBuyerOwnership(order.getBuyerId());

        // 3. Find or generate Escrow Virtual Account for this order
        EscrowAccount escrow = escrowAccountRepository.findByOrderId(orderId)
                .orElseGet(() -> {
                    String cleanOrderId = orderId.replace("-", "").toUpperCase();
                    String vaNumber = "KFPCL" + cleanOrderId;
                    EscrowAccount newAccount = EscrowAccount.builder()
                            .orderId(orderId)
                            .virtualAccountNumber(vaNumber)
                            .ifscCode("KFPCL00001")
                            .totalAmountLocked(order.getGrandTotal())
                            .releaseStatus(EscrowReleaseStatus.LOCKED)
                            .lockedAt(LocalDateTime.now())
                            .build();
                    return escrowAccountRepository.save(newAccount);
                });

        // 4. Retrieve current payment transaction status if any
        Optional<PaymentTransaction> txnOpt = transactionRepository.findByOrderId(orderId);
        PaymentStatus currentStatus = txnOpt.map(PaymentTransaction::getStatus).orElse(PaymentStatus.PENDING_PAYMENT);

        // Note: This API must NOT mark an order as paid.

        return VirtualAccountResponse.builder()
                .orderId(orderId)
                .beneficiaryName("KFPCL B2B Marketplace Escrow")
                .virtualAccountNumber(escrow.getVirtualAccountNumber())
                .ifscCode(escrow.getIfscCode())
                .bankName("KFPCL Escrow Partner Bank")
                .amount(order.getGrandTotal())
                .currency(order.getCurrency())
                .escrowStatus(escrow.getReleaseStatus())
                .paymentStatus(currentStatus)
                .instructions("Transfer funds via NEFT/RTGS/IMPS to this dedicated Escrow Virtual Account. After transfer, submit bank UTR via /api/v1/payments/bank-transfer/confirm.")
                .build();
    }
}
