package com.payment.service.impl;

import com.payment.dto.bank.BankTransferConfirmRequest;
import com.payment.dto.bank.BankTransferConfirmResponse;
import com.payment.entity.PaymentTransaction;
import com.payment.entity.enums.PaymentGatewayType;
import com.payment.entity.enums.PaymentMethod;
import com.payment.entity.enums.PaymentStatus;
import com.payment.exception.BadRequestException;
import com.payment.exception.DuplicateResourceException;
import com.payment.integration.order.OrderDetailsDto;
import com.payment.integration.order.OrderPaymentClient;
import com.payment.repository.PaymentTransactionRepository;
import com.payment.security.SecurityUtils;
import com.payment.security.UserContext;
import com.payment.service.BankTransferService;
import com.payment.service.audit.PaymentAuditService;
import com.payment.service.state.PaymentStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankTransferServiceImpl implements BankTransferService {

    private final PaymentTransactionRepository transactionRepository;
    private final OrderPaymentClient orderPaymentClient;
    private final SecurityUtils securityUtils;
    private final PaymentStateMachine stateMachine;
    private final PaymentAuditService auditService;

    @Override
    @Transactional
    public BankTransferConfirmResponse confirmBankTransfer(BankTransferConfirmRequest request, String clientIp) {
        UserContext currentUser = securityUtils.getCurrentUser();
        log.info("Processing bank transfer confirmation for orderId: {}, UTR: {}, user: {}",
                request.getOrderId(), request.getUtrNumber(), currentUser.getUserId());

        // 1. Fetch trusted order details from Order module
        OrderDetailsDto order = orderPaymentClient.getOrderDetails(request.getOrderId());
        if (order == null) {
            throw new BadRequestException("Order not found with ID: " + request.getOrderId());
        }

        // 2. Validate that the authenticated buyer owns the order
        securityUtils.verifyBuyerOwnership(order.getBuyerId());

        // 3. Ensure UTR number is unique (prevents duplicate UTR re-use)
        if (transactionRepository.existsByUtrNumber(request.getUtrNumber())) {
            throw new DuplicateResourceException("UTR number '" + request.getUtrNumber() + "' has already been submitted for another transaction.");
        }

        // 4. Compare submitted amount with server-side trusted order amount
        if (order.getGrandTotal().compareTo(request.getAmount()) != 0) {
            throw new BadRequestException(String.format(
                    "Amount mismatch: Submitted amount (%s %s) does not match required order amount (%s %s)",
                    order.getCurrency(), request.getAmount(), order.getCurrency(), order.getGrandTotal()
            ));
        }

        // 5. Find existing transaction or create new one
        PaymentTransaction transaction = transactionRepository.findByOrderId(request.getOrderId())
                .orElseGet(() -> {
                    String txnRef = "TXN-BNK-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
                    return PaymentTransaction.builder()
                            .transactionReference(txnRef)
                            .orderId(order.getOrderId())
                            .buyerId(order.getBuyerId())
                            .sellerId(order.getSellerId())
                            .paymentMethod(PaymentMethod.BANK)
                            .gateway(PaymentGatewayType.NONE)
                            .amount(order.getGrandTotal())
                            .currency(order.getCurrency())
                            .status(PaymentStatus.PENDING_PAYMENT)
                            .recipientEmail(order.getCustomerEmail())
                            .build();
                });

        // 6. Validate state transition to PAYMENT_PROCESSING
        PaymentStatus previousStatus = transaction.getStatus();
        stateMachine.validateTransition(previousStatus, PaymentStatus.PAYMENT_PROCESSING);

        // 7. Update transaction fields with bank transfer info
        transaction.setUtrNumber(request.getUtrNumber());
        transaction.setRemitterBank(request.getRemitterBank());
        transaction.setTransferDate(request.getTransferDate());
        transaction.setReceiptDocumentUrl(request.getReceiptDocUrl());
        transaction.setPaymentMethod(PaymentMethod.BANK);
        transaction.setStatus(PaymentStatus.PAYMENT_PROCESSING);

        PaymentTransaction savedTxn = transactionRepository.save(transaction);

        // 8. Note: Do NOT lock escrow merely because the buyer submitted a UTR.
        // Official bank reconciliation webhook must confirm the funds first.

        // 9. Audit Log
        auditService.logAction(
                savedTxn.getId(),
                savedTxn.getOrderId(),
                "SUBMIT_BANK_TRANSFER_UTR",
                previousStatus != null ? previousStatus.name() : null,
                PaymentStatus.PAYMENT_PROCESSING.name(),
                currentUser.getUserId(),
                "BANK_TRANSFER",
                clientIp,
                "UTR submitted: " + request.getUtrNumber() + ", Bank: " + request.getRemitterBank()
        );

        return BankTransferConfirmResponse.builder()
                .orderId(savedTxn.getOrderId())
                .transactionReference(savedTxn.getTransactionReference())
                .utrNumber(savedTxn.getUtrNumber())
                .remitterBank(savedTxn.getRemitterBank())
                .amount(savedTxn.getAmount())
                .currency(savedTxn.getCurrency())
                .paymentStatus(savedTxn.getStatus())
                .escrowLocked(false)
                .transferDate(savedTxn.getTransferDate())
                .submittedAt(LocalDateTime.now())
                .message("Bank transfer UTR received and set to PAYMENT_PROCESSING. Escrow will lock upon bank reconciliation confirmation.")
                .build();
    }
}
