package com.payment.service.impl;

import com.payment.dto.gateway.VerifyPaymentRequest;
import com.payment.dto.gateway.VerifyPaymentResponse;
import com.payment.entity.EscrowAccount;
import com.payment.entity.Invoice;
import com.payment.entity.PaymentTransaction;
import com.payment.entity.enums.EscrowReleaseStatus;
import com.payment.entity.enums.InvoiceType;
import com.payment.entity.enums.PaymentStatus;
import com.payment.event.DispatchAllowedEvent;
import com.payment.event.EscrowLockedEvent;
import com.payment.event.PaymentVerifiedEvent;
import com.payment.exception.BadRequestException;
import com.payment.exception.ResourceNotFoundException;
import com.payment.gateway.PaymentGateway;
import com.payment.gateway.PaymentGatewayFactory;
import com.payment.integration.order.OrderPaymentClient;
import com.payment.repository.EscrowAccountRepository;
import com.payment.repository.PaymentTransactionRepository;
import com.payment.security.SecurityUtils;
import com.payment.security.UserContext;
import com.payment.service.InvoiceService;
import com.payment.service.PaymentVerificationService;
import com.payment.service.audit.PaymentAuditService;
import com.payment.service.state.PaymentStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentVerificationServiceImpl implements PaymentVerificationService {

    private final PaymentTransactionRepository transactionRepository;
    private final EscrowAccountRepository escrowAccountRepository;
    private final PaymentGatewayFactory gatewayFactory;
    private final PaymentStateMachine stateMachine;
    private final InvoiceService invoiceService;
    private final OrderPaymentClient orderPaymentClient;
    private final SecurityUtils securityUtils;
    private final PaymentAuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public VerifyPaymentResponse verifyPayment(VerifyPaymentRequest request, String clientIp) {
        UserContext currentUser = securityUtils.getCurrentUser();
        log.info("Verifying payment for orderId: {}, paymentId: {}, user: {}",
                request.getOrderId(), request.getPaymentId(), currentUser.getUserId());

        // 1. Locate existing Payment Transaction
        PaymentTransaction transaction = transactionRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("No payment transaction found for order: " + request.getOrderId()));

        // 2. Verify Buyer Ownership
        securityUtils.verifyBuyerOwnership(transaction.getBuyerId());

        // 3. Idempotency Check: if already verified and in locked/dispatch state, return current status
        if (transaction.getStatus() == PaymentStatus.DISPATCH_ALLOWED ||
                transaction.getStatus() == PaymentStatus.ESCROW_LOCKED ||
                transaction.getStatus() == PaymentStatus.FUNDS_RELEASED) {
            log.info("Payment for order {} already verified. Returning existing state.", request.getOrderId());
            EscrowAccount escrow = escrowAccountRepository.findByOrderId(request.getOrderId()).orElse(null);
            Invoice invoice = invoiceService.generateOrGetInvoice(transaction, InvoiceType.TAX_INVOICE);
            return buildResponse(transaction, escrow, invoice, true, "Payment already verified successfully (Idempotent)");
        }

        // 4. Confirm Gateway Order ID matches the stored transaction
        if (transaction.getGatewayOrderId() == null || !transaction.getGatewayOrderId().equals(request.getGatewayOrderId())) {
            throw new BadRequestException(String.format(
                    "Gateway order ID mismatch. Expected: '%s', Received: '%s'",
                    transaction.getGatewayOrderId(), request.getGatewayOrderId()
            ));
        }

        // 5. Server-Side Signature Verification using Gateway Secret (never trust frontend)
        PaymentGateway gateway = gatewayFactory.getGateway(transaction.getGateway());
        boolean isValidSignature = gateway.verifySignature(
                request.getGatewayOrderId(),
                request.getPaymentId(),
                request.getSignature()
        );

        if (!isValidSignature) {
            log.warn("Invalid signature received for order: {}, paymentId: {}", request.getOrderId(), request.getPaymentId());
            throw new BadRequestException("Invalid payment signature. Verification failed.");
        }

        // 6. Enforce State Transitions (PENDING_PAYMENT -> PAYMENT_PROCESSING -> ESCROW_LOCKED -> DISPATCH_ALLOWED)
        stateMachine.validateTransition(transaction.getStatus(), PaymentStatus.PAYMENT_PROCESSING);
        stateMachine.validateTransition(PaymentStatus.PAYMENT_PROCESSING, PaymentStatus.ESCROW_LOCKED);
        stateMachine.validateTransition(PaymentStatus.ESCROW_LOCKED, PaymentStatus.DISPATCH_ALLOWED);

        // Update Transaction
        PaymentStatus previousStatus = transaction.getStatus();
        transaction.setGatewayPaymentId(request.getPaymentId());
        transaction.setStatus(PaymentStatus.DISPATCH_ALLOWED);
        PaymentTransaction updatedTxn = transactionRepository.save(transaction);

        // 7. Create and Lock Escrow Account
        EscrowAccount escrow = escrowAccountRepository.findByOrderId(request.getOrderId())
                .orElseGet(() -> {
                    String vaNumber = "VA-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
                    EscrowAccount newEscrow = EscrowAccount.builder()
                            .orderId(transaction.getOrderId())
                            .paymentTransactionId(transaction.getId())
                            .virtualAccountNumber(vaNumber)
                            .ifscCode("KFPCL00001")
                            .totalAmountLocked(transaction.getAmount())
                            .releaseStatus(EscrowReleaseStatus.LOCKED)
                            .lockedAt(LocalDateTime.now())
                            .build();
                    return escrowAccountRepository.save(newEscrow);
                });

        if (escrow.getReleaseStatus() != EscrowReleaseStatus.LOCKED) {
            escrow.setReleaseStatus(EscrowReleaseStatus.LOCKED);
            escrow.setLockedAt(LocalDateTime.now());
            escrow = escrowAccountRepository.save(escrow);
        }

        // 8. Generate GST Tax Invoice
        Invoice invoice = invoiceService.generateOrGetInvoice(updatedTxn, InvoiceType.TAX_INVOICE);

        // 9. Update Order Module Payment State
        orderPaymentClient.updateOrderPaymentStatus(request.getOrderId(), "PAID");

        // 10. Publish Domain Events
        LocalDateTime now = LocalDateTime.now();
        eventPublisher.publishEvent(PaymentVerifiedEvent.builder()
                .orderId(transaction.getOrderId())
                .transactionId(transaction.getId())
                .transactionReference(transaction.getTransactionReference())
                .buyerId(transaction.getBuyerId())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .timestamp(now)
                .build());

        eventPublisher.publishEvent(EscrowLockedEvent.builder()
                .orderId(transaction.getOrderId())
                .escrowAccountId(escrow.getId())
                .virtualAccountNumber(escrow.getVirtualAccountNumber())
                .totalAmountLocked(escrow.getTotalAmountLocked())
                .lockedAt(now)
                .build());

        eventPublisher.publishEvent(DispatchAllowedEvent.builder()
                .orderId(transaction.getOrderId())
                .transactionId(transaction.getId())
                .reason("Payment verified via Razorpay gateway and Escrow locked")
                .timestamp(now)
                .build());

        // 11. Write Audit Log
        auditService.logAction(
                transaction.getId(),
                transaction.getOrderId(),
                "VERIFY_PAYMENT_AND_LOCK_ESCROW",
                previousStatus.name(),
                PaymentStatus.DISPATCH_ALLOWED.name(),
                currentUser.getUserId(),
                transaction.getGateway().name(),
                clientIp,
                "Signature verified successfully. Escrow locked. Invoice: " + invoice.getInvoiceNumber()
        );

        return buildResponse(updatedTxn, escrow, invoice, false, "Payment verified and Escrow locked successfully. Dispatch authorized.");
    }

    private VerifyPaymentResponse buildResponse(PaymentTransaction txn, EscrowAccount escrow, Invoice invoice, boolean isReplay, String message) {
        return VerifyPaymentResponse.builder()
                .orderId(txn.getOrderId())
                .transactionReference(txn.getTransactionReference())
                .gatewayPaymentId(txn.getGatewayPaymentId())
                .amount(txn.getAmount())
                .currency(txn.getCurrency())
                .paymentStatus(txn.getStatus())
                .escrowLocked(escrow != null && escrow.getReleaseStatus() == EscrowReleaseStatus.LOCKED)
                .escrowStatus(escrow != null ? escrow.getReleaseStatus() : null)
                .virtualAccountNumber(escrow != null ? escrow.getVirtualAccountNumber() : null)
                .invoiceNumber(invoice != null ? invoice.getInvoiceNumber() : null)
                .dispatchAllowed(txn.getStatus() == PaymentStatus.DISPATCH_ALLOWED)
                .verifiedAt(LocalDateTime.now())
                .message(message)
                .build();
    }
}
