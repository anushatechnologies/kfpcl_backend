package com.payment.service.impl;

import com.payment.entity.EscrowAccount;
import com.payment.entity.PaymentTransaction;
import com.payment.entity.enums.DisputeStatus;
import com.payment.entity.enums.EscrowReleaseStatus;
import com.payment.entity.enums.PaymentStatus;
import com.payment.event.EscrowLockedEvent;
import com.payment.event.FundsReleasedEvent;
import com.payment.exception.BadRequestException;
import com.payment.exception.InvalidPaymentStateException;
import com.payment.exception.ResourceNotFoundException;
import com.payment.integration.order.OrderDetailsDto;
import com.payment.integration.order.OrderPaymentClient;
import com.payment.repository.EscrowAccountRepository;
import com.payment.repository.PaymentDisputeRepository;
import com.payment.repository.PaymentTransactionRepository;
import com.payment.service.EscrowService;
import com.payment.service.PaymentStateTransitionService;
import com.payment.service.audit.PaymentAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EscrowServiceImpl implements EscrowService {

    private final EscrowAccountRepository escrowAccountRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final PaymentDisputeRepository disputeRepository;
    private final OrderPaymentClient orderPaymentClient;
    private final PaymentStateTransitionService stateTransitionService;
    private final PaymentAuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public EscrowAccount lockEscrow(String orderId, Long transactionId, BigDecimal amount,
                                   String virtualAccountNumber, String ifscCode) {
        log.info("Locking escrow account for orderId: {}, amount: {}", orderId, amount);

        EscrowAccount escrow = escrowAccountRepository.findByOrderId(orderId)
                .orElseGet(() -> EscrowAccount.builder()
                        .orderId(orderId)
                        .virtualAccountNumber(virtualAccountNumber != null ? virtualAccountNumber : "KFPCL" + orderId.replace("-", ""))
                        .ifscCode(ifscCode != null ? ifscCode : "KFPCL00001")
                        .build());

        escrow.setPaymentTransactionId(transactionId);
        escrow.setTotalAmountLocked(amount);
        escrow.setReleaseStatus(EscrowReleaseStatus.LOCKED);
        escrow.setLockedAt(LocalDateTime.now());

        EscrowAccount savedEscrow = escrowAccountRepository.save(escrow);

        eventPublisher.publishEvent(new EscrowLockedEvent(
                orderId,
                savedEscrow.getId(),
                savedEscrow.getVirtualAccountNumber(),
                amount,
                savedEscrow.getLockedAt()
        ));

        return savedEscrow;
    }

    @Override
    @Transactional
    public EscrowAccount releaseFundsOnDelivery(String orderId, String clientIp) {
        log.info("Attempting escrow release on delivery for orderId: {}", orderId);

        EscrowAccount escrow = escrowAccountRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Escrow account not found for order: " + orderId));

        if (escrow.getReleaseStatus() == EscrowReleaseStatus.REFUNDED_TO_BUYER) {
            throw new InvalidPaymentStateException("Cannot release funds: escrow has already been refunded to buyer.");
        }
        if (escrow.getReleaseStatus() == EscrowReleaseStatus.RELEASED_TO_SELLER) {
            log.info("Escrow funds already released to seller for order: {}", orderId);
            return escrow; // Idempotent
        }
        if (escrow.getReleaseStatus() != EscrowReleaseStatus.LOCKED) {
            throw new InvalidPaymentStateException("Cannot release funds: escrow is not in LOCKED status. Current status: " + escrow.getReleaseStatus());
        }

        // Check if an open dispute exists
        if (disputeRepository.existsByOrderIdAndStatusIn(orderId, List.of(DisputeStatus.OPEN, DisputeStatus.UNDER_REVIEW))) {
            throw new InvalidPaymentStateException("Cannot release funds: order is currently under active dispute.");
        }

        // Verify delivery status from order module
        OrderDetailsDto order = orderPaymentClient.getOrderDetails(orderId);
        if (order != null && "CANCELLED".equalsIgnoreCase(order.getOrderStatus())) {
            throw new BadRequestException("Cannot release funds: order has been cancelled.");
        }

        LocalDateTime now = LocalDateTime.now();
        escrow.setReleaseStatus(EscrowReleaseStatus.RELEASED_TO_SELLER);
        escrow.setReleasedAt(now);
        EscrowAccount updatedEscrow = escrowAccountRepository.save(escrow);

        // Transition Payment Transaction to FUNDS_RELEASED
        transactionRepository.findByOrderId(orderId).ifPresent(txn -> {
            stateTransitionService.transition(
                    txn,
                    PaymentStatus.FUNDS_RELEASED,
                    "SYSTEM_DELIVERY",
                    "ESCROW_SERVICE",
                    clientIp,
                    "Delivery completed/accepted. Escrow funds released to seller."
            );
        });

        eventPublisher.publishEvent(FundsReleasedEvent.builder()
                .orderId(orderId)
                .sellerId(order != null ? order.getSellerId() : "SELLER")
                .netAmount(updatedEscrow.getTotalAmountLocked())
                .bankReference("ESCROW-REL-" + orderId)
                .timestamp(now)
                .build());

        auditService.logAction(
                updatedEscrow.getPaymentTransactionId(),
                orderId,
                "ESCROW_FUNDS_RELEASED",
                EscrowReleaseStatus.LOCKED.name(),
                EscrowReleaseStatus.RELEASED_TO_SELLER.name(),
                "SYSTEM",
                "ESCROW_SERVICE",
                clientIp != null ? clientIp : "127.0.0.1",
                "Escrow funds released to seller for order " + orderId
        );

        log.info("Escrow funds of amount {} successfully released to seller for order {}",
                updatedEscrow.getTotalAmountLocked(), orderId);

        return updatedEscrow;
    }

    @Override
    @Transactional
    public EscrowAccount refundEscrow(String orderId, String clientIp) {
        log.info("Attempting escrow refund to buyer for orderId: {}", orderId);

        EscrowAccount escrow = escrowAccountRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Escrow account not found for order: " + orderId));

        if (escrow.getReleaseStatus() == EscrowReleaseStatus.RELEASED_TO_SELLER) {
            throw new InvalidPaymentStateException("Cannot refund escrow: funds have already been released to seller.");
        }
        if (escrow.getReleaseStatus() == EscrowReleaseStatus.REFUNDED_TO_BUYER) {
            log.info("Escrow already refunded to buyer for order: {}", orderId);
            return escrow; // Idempotent
        }

        LocalDateTime now = LocalDateTime.now();
        escrow.setReleaseStatus(EscrowReleaseStatus.REFUNDED_TO_BUYER);
        escrow.setRefundedAt(now);
        EscrowAccount updatedEscrow = escrowAccountRepository.save(escrow);

        auditService.logAction(
                updatedEscrow.getPaymentTransactionId(),
                orderId,
                "ESCROW_REFUNDED_TO_BUYER",
                EscrowReleaseStatus.LOCKED.name(),
                EscrowReleaseStatus.REFUNDED_TO_BUYER.name(),
                "FINANCE",
                "ESCROW_SERVICE",
                clientIp != null ? clientIp : "127.0.0.1",
                "Escrow refunded to buyer for order " + orderId
        );

        return updatedEscrow;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EscrowAccount> getEscrowByOrderId(String orderId) {
        return escrowAccountRepository.findByOrderId(orderId);
    }
}
