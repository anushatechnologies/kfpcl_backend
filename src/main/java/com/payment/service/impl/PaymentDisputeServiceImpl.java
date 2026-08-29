package com.payment.service.impl;

import com.payment.dto.dispute.DisputeResponse;
import com.payment.dto.dispute.RaiseDisputeRequest;
import com.payment.entity.PaymentDispute;
import com.payment.entity.PaymentTransaction;
import com.payment.entity.enums.DisputeStatus;
import com.payment.entity.enums.PaymentStatus;
import com.payment.event.PaymentDisputedEvent;
import com.payment.exception.DuplicateResourceException;
import com.payment.exception.ResourceNotFoundException;
import com.payment.integration.order.OrderDetailsDto;
import com.payment.integration.order.OrderPaymentClient;
import com.payment.repository.PaymentDisputeRepository;
import com.payment.repository.PaymentTransactionRepository;
import com.payment.security.SecurityUtils;
import com.payment.security.UserContext;
import com.payment.service.PaymentDisputeService;
import com.payment.service.audit.PaymentAuditService;
import com.payment.service.state.PaymentStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentDisputeServiceImpl implements PaymentDisputeService {

    private final PaymentDisputeRepository disputeRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final OrderPaymentClient orderPaymentClient;
    private final PaymentStateMachine stateMachine;
    private final SecurityUtils securityUtils;
    private final PaymentAuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public DisputeResponse raiseDispute(RaiseDisputeRequest request, String clientIp) {
        UserContext currentUser = securityUtils.getCurrentUser();
        String orderId = request.getOrderId();
        log.info("Raising dispute for orderId: {}, user: {}, role: {}", orderId, currentUser.getUserId(), currentUser.getRole());

        OrderDetailsDto order = orderPaymentClient.getOrderDetails(orderId);
        if (order == null) {
            throw new ResourceNotFoundException("Order not found with ID: " + orderId);
        }

        // Verify caller is a legitimate participant
        securityUtils.verifyOrderParticipant(order.getBuyerId(), order.getSellerId());

        // Check if an active dispute is already open
        if (disputeRepository.existsByOrderIdAndStatusIn(orderId, List.of(DisputeStatus.OPEN, DisputeStatus.UNDER_REVIEW))) {
            throw new DuplicateResourceException("An active dispute is already open for order: " + orderId);
        }

        PaymentTransaction txn = transactionRepository.findByOrderId(orderId).orElse(null);
        PaymentStatus prevStatus = null;
        Long txnId = null;

        if (txn != null) {
            prevStatus = txn.getStatus();
            txnId = txn.getId();
            // Validate and transition payment state to DISPUTED
            stateMachine.validateTransition(txn.getStatus(), PaymentStatus.DISPUTED);
            txn.setStatus(PaymentStatus.DISPUTED);
            transactionRepository.save(txn);
        }

        String evidenceStr = request.getEvidenceUrls() != null && !request.getEvidenceUrls().isEmpty()
                ? String.join(",", request.getEvidenceUrls())
                : null;

        PaymentDispute dispute = PaymentDispute.builder()
                .orderId(orderId)
                .transactionId(txnId)
                .raisedByUserId(currentUser.getUserId())
                .raisedByRole(currentUser.getRole())
                .reason(request.getReason())
                .status(DisputeStatus.OPEN)
                .evidenceUrls(evidenceStr)
                .build();

        PaymentDispute savedDispute = disputeRepository.save(dispute);

        // Record audit trail
        auditService.logAction(
                txnId,
                orderId,
                "PAYMENT_DISPUTED",
                prevStatus != null ? prevStatus.name() : null,
                PaymentStatus.DISPUTED.name(),
                currentUser.getUserId(),
                "DISPUTE_MODULE",
                clientIp != null ? clientIp : "127.0.0.1",
                "Dispute #" + savedDispute.getId() + " raised: " + request.getReason()
        );

        // Publish domain event
        eventPublisher.publishEvent(PaymentDisputedEvent.builder()
                .disputeId(savedDispute.getId())
                .orderId(orderId)
                .reason(request.getReason())
                .raisedByUserId(currentUser.getUserId())
                .timestamp(java.time.LocalDateTime.now())
                .build());

        log.info("Dispute #{} raised and escrow funds frozen in DISPUTED state for order: {}",
                savedDispute.getId(), orderId);

        List<String> evidenceList = evidenceStr != null
                ? Arrays.asList(evidenceStr.split(","))
                : List.of();

        return DisputeResponse.builder()
                .disputeId(savedDispute.getId())
                .orderId(orderId)
                .transactionId(txnId)
                .raisedByUserId(currentUser.getUserId())
                .raisedByRole(currentUser.getRole())
                .reason(savedDispute.getReason())
                .disputeStatus(savedDispute.getStatus())
                .paymentStatus(PaymentStatus.DISPUTED)
                .evidenceUrls(evidenceList)
                .createdAt(savedDispute.getCreatedAt())
                .message("Dispute raised successfully. Escrow funds are frozen under DISPUTED status pending resolution.")
                .build();
    }
}
