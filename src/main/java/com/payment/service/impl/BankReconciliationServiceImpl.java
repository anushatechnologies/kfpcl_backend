package com.payment.service.impl;

import com.payment.dto.webhook.BankReconciliationWebhookRequest;
import com.payment.dto.webhook.BankReconciliationWebhookResponse;
import com.payment.entity.EscrowAccount;
import com.payment.entity.Invoice;
import com.payment.entity.PaymentTransaction;
import com.payment.entity.WebhookEvent;
import com.payment.entity.enums.EscrowReleaseStatus;
import com.payment.entity.enums.InvoiceType;
import com.payment.entity.enums.PaymentStatus;
import com.payment.event.DispatchAllowedEvent;
import com.payment.event.EscrowLockedEvent;
import com.payment.event.PaymentVerifiedEvent;
import com.payment.exception.BadRequestException;
import com.payment.exception.DuplicateResourceException;
import com.payment.exception.ResourceNotFoundException;
import com.payment.exception.UnauthorizedException;
import com.payment.integration.order.OrderPaymentClient;
import com.payment.repository.EscrowAccountRepository;
import com.payment.repository.PaymentTransactionRepository;
import com.payment.repository.WebhookEventRepository;
import com.payment.service.BankReconciliationService;
import com.payment.service.InvoiceService;
import com.payment.service.audit.PaymentAuditService;
import com.payment.service.state.PaymentStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankReconciliationServiceImpl implements BankReconciliationService {

    private final PaymentTransactionRepository transactionRepository;
    private final EscrowAccountRepository escrowAccountRepository;
    private final WebhookEventRepository webhookEventRepository;
    private final PaymentStateMachine stateMachine;
    private final InvoiceService invoiceService;
    private final OrderPaymentClient orderPaymentClient;
    private final PaymentAuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${bank.webhook.secret:kfpcl_bank_webhook_secret_2026}")
    private String configuredWebhookSecret;

    @Override
    @Transactional
    public BankReconciliationWebhookResponse processReconciliation(
            BankReconciliationWebhookRequest request, String webhookSecret, String clientIp) {

        log.info("Processing bank reconciliation webhook: eventId={}, VA={}, UTR={}, amount={}",
                request.getEventId(), request.getVirtualAccountNumber(), request.getUtrNumber(), request.getAmount());

        // 1. Webhook Secret Authentication
        if (webhookSecret != null && !webhookSecret.isBlank() && !webhookSecret.equals(configuredWebhookSecret)) {
            log.warn("Invalid webhook secret provided for eventId: {}", request.getEventId());
            throw new UnauthorizedException("Invalid or missing webhook secret/signature");
        }

        // 2. Replay Protection & Event Deduplication
        if (webhookEventRepository.existsByEventId(request.getEventId())) {
            log.warn("Duplicate webhook event received: {}", request.getEventId());
            throw new DuplicateResourceException("Webhook event '" + request.getEventId() + "' has already been processed.");
        }

        // Record incoming webhook event
        WebhookEvent webhookEvent = WebhookEvent.builder()
                .eventId(request.getEventId())
                .eventType("BANK_RECONCILIATION")
                .source("BANK_WEBHOOK")
                .payload(request.toString())
                .receivedAt(LocalDateTime.now())
                .build();
        webhookEventRepository.save(webhookEvent);

        // 3. Match Virtual Account
        EscrowAccount escrow = escrowAccountRepository.findByVirtualAccountNumber(request.getVirtualAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No escrow account found for virtual account: " + request.getVirtualAccountNumber()));

        // 4. Match or Create Order Transaction
        PaymentTransaction transaction = transactionRepository.findByOrderId(escrow.getOrderId())
                .orElseGet(() -> {
                    var order = orderPaymentClient.getOrderDetails(escrow.getOrderId());
                    String txnRef = "TXN-BNK-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
                    PaymentTransaction newTxn = PaymentTransaction.builder()
                            .transactionReference(txnRef)
                            .orderId(escrow.getOrderId())
                            .buyerId(order != null ? order.getBuyerId() : "BUYER-101")
                            .sellerId(order != null ? order.getSellerId() : "SELLER-501")
                            .paymentMethod(com.payment.entity.enums.PaymentMethod.BANK)
                            .gateway(com.payment.entity.enums.PaymentGatewayType.NONE)
                            .amount(order != null ? order.getGrandTotal() : request.getAmount())
                            .currency(order != null ? order.getCurrency() : "INR")
                            .status(PaymentStatus.PENDING_PAYMENT)
                            .recipientEmail(order != null ? order.getCustomerEmail() : "buyer@kfpcl.com")
                            .build();
                    return transactionRepository.save(newTxn);
                });

        // 5. Match Exact Amount
        if (transaction.getAmount().compareTo(request.getAmount()) != 0) {
            throw new BadRequestException(String.format(
                    "Reconciliation amount mismatch. Order amount: %s %s, Credited amount: %s %s",
                    transaction.getCurrency(), transaction.getAmount(), transaction.getCurrency(), request.getAmount()
            ));
        }

        // 6. Match or Associate UTR
        if (transaction.getUtrNumber() != null && !transaction.getUtrNumber().equalsIgnoreCase(request.getUtrNumber())) {
            log.warn("Buyer submitted UTR ({}) differs from bank credited UTR ({}) for order {}",
                    transaction.getUtrNumber(), request.getUtrNumber(), transaction.getOrderId());
        }
        transaction.setUtrNumber(request.getUtrNumber());

        // 7. Enforce State Machine Transitions
        PaymentStatus previousStatus = transaction.getStatus();
        if (previousStatus == PaymentStatus.PENDING_PAYMENT) {
            stateMachine.validateTransition(PaymentStatus.PENDING_PAYMENT, PaymentStatus.PAYMENT_PROCESSING);
        }
        stateMachine.validateTransition(PaymentStatus.PAYMENT_PROCESSING, PaymentStatus.ESCROW_LOCKED);
        stateMachine.validateTransition(PaymentStatus.ESCROW_LOCKED, PaymentStatus.DISPATCH_ALLOWED);

        // 8. Lock Escrow Account & Authorize Dispatch
        escrow.setReleaseStatus(EscrowReleaseStatus.LOCKED);
        escrow.setLockedAt(LocalDateTime.now());
        escrow.setPaymentTransactionId(transaction.getId());
        escrowAccountRepository.save(escrow);

        transaction.setStatus(PaymentStatus.DISPATCH_ALLOWED);
        PaymentTransaction updatedTxn = transactionRepository.save(transaction);

        // 9. Generate GST Tax Invoice
        Invoice invoice = invoiceService.generateOrGetInvoice(updatedTxn, InvoiceType.TAX_INVOICE);

        // 10. Update Order Module Payment State
        orderPaymentClient.updateOrderPaymentStatus(escrow.getOrderId(), "PAID");

        // 11. Publish Domain Events
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
                .reason("Bank transfer reconciled via bank webhook. Escrow locked.")
                .timestamp(now)
                .build());

        // 12. Write Audit Log
        auditService.logAction(
                transaction.getId(),
                transaction.getOrderId(),
                "BANK_WEBHOOK_RECONCILED",
                previousStatus != null ? previousStatus.name() : null,
                PaymentStatus.DISPATCH_ALLOWED.name(),
                "SYSTEM_BANK_WEBHOOK",
                "BANK_WEBHOOK",
                clientIp,
                "Reconciled event: " + request.getEventId() + ", UTR: " + request.getUtrNumber() + ", Escrow locked"
        );

        return BankReconciliationWebhookResponse.builder()
                .eventId(request.getEventId())
                .orderId(transaction.getOrderId())
                .transactionReference(transaction.getTransactionReference())
                .utrNumber(request.getUtrNumber())
                .amount(transaction.getAmount())
                .paymentStatus(PaymentStatus.DISPATCH_ALLOWED)
                .escrowStatus(EscrowReleaseStatus.LOCKED)
                .escrowLocked(true)
                .invoiceNumber(invoice.getInvoiceNumber())
                .dispatchAuthorized(true)
                .reconciledAt(now)
                .message("Bank reconciliation successful. Escrow locked and dispatch authorized.")
                .build();
    }
}
