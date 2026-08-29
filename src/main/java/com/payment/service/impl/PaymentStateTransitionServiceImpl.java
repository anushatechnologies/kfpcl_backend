package com.payment.service.impl;

import com.payment.entity.PaymentTransaction;
import com.payment.entity.enums.PaymentStatus;
import com.payment.repository.PaymentTransactionRepository;
import com.payment.service.PaymentStateTransitionService;
import com.payment.service.audit.PaymentAuditService;
import com.payment.service.state.PaymentStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentStateTransitionServiceImpl implements PaymentStateTransitionService {

    private final PaymentStateMachine stateMachine;
    private final PaymentTransactionRepository transactionRepository;
    private final PaymentAuditService auditService;

    @Override
    public void validateTransition(PaymentStatus currentStatus, PaymentStatus nextStatus) {
        stateMachine.validateTransition(currentStatus, nextStatus);
    }

    @Override
    @Transactional
    public PaymentTransaction transition(PaymentTransaction transaction, PaymentStatus nextStatus,
                                        String performedBy, String source, String clientIp, String reason) {
        PaymentStatus prevStatus = transaction.getStatus();
        log.info("Transitioning order {} payment state from {} -> {}", transaction.getOrderId(), prevStatus, nextStatus);

        stateMachine.validateTransition(prevStatus, nextStatus);
        transaction.setStatus(nextStatus);
        PaymentTransaction updated = transactionRepository.save(transaction);

        auditService.logAction(
                updated.getId(),
                updated.getOrderId(),
                "STATE_TRANSITION",
                prevStatus != null ? prevStatus.name() : null,
                nextStatus.name(),
                performedBy != null ? performedBy : "SYSTEM",
                source != null ? source : "PAYMENT_STATE_SERVICE",
                clientIp != null ? clientIp : "127.0.0.1",
                reason != null ? reason : "Payment status transitioned to " + nextStatus
        );

        return updated;
    }
}
