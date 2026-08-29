package com.payment.service.state;

import com.payment.entity.enums.PaymentStatus;
import com.payment.exception.InvalidPaymentStateException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class PaymentStateMachine {

    private static final Map<PaymentStatus, Set<PaymentStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(PaymentStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(PaymentStatus.PENDING_PAYMENT,
                EnumSet.of(PaymentStatus.PAYMENT_PROCESSING, PaymentStatus.CANCELLED, PaymentStatus.DISPUTED));

        ALLOWED_TRANSITIONS.put(PaymentStatus.PAYMENT_PROCESSING,
                EnumSet.of(PaymentStatus.ESCROW_LOCKED, PaymentStatus.FAILED, PaymentStatus.REFUNDED, PaymentStatus.DISPUTED));

        ALLOWED_TRANSITIONS.put(PaymentStatus.ESCROW_LOCKED,
                EnumSet.of(PaymentStatus.DISPATCH_ALLOWED, PaymentStatus.DISPUTED, PaymentStatus.REFUNDED));

        ALLOWED_TRANSITIONS.put(PaymentStatus.DISPATCH_ALLOWED,
                EnumSet.of(PaymentStatus.FUNDS_RELEASED, PaymentStatus.DISPUTED));

        ALLOWED_TRANSITIONS.put(PaymentStatus.DISPUTED,
                EnumSet.of(PaymentStatus.FUNDS_RELEASED, PaymentStatus.REFUNDED, PaymentStatus.DISPATCH_ALLOWED));

        // Terminal states
        ALLOWED_TRANSITIONS.put(PaymentStatus.FUNDS_RELEASED, EnumSet.noneOf(PaymentStatus.class));
        ALLOWED_TRANSITIONS.put(PaymentStatus.REFUNDED, EnumSet.noneOf(PaymentStatus.class));
        ALLOWED_TRANSITIONS.put(PaymentStatus.FAILED, EnumSet.noneOf(PaymentStatus.class));
        ALLOWED_TRANSITIONS.put(PaymentStatus.CANCELLED, EnumSet.noneOf(PaymentStatus.class));
    }

    public void validateTransition(PaymentStatus currentStatus, PaymentStatus nextStatus) {
        if (currentStatus == nextStatus) {
            return; // Idempotent or no-op
        }

        Set<PaymentStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, EnumSet.noneOf(PaymentStatus.class));
        if (!allowed.contains(nextStatus)) {
            throw new InvalidPaymentStateException(String.format(
                    "Invalid payment state transition from '%s' to '%s'. Allowed transitions: %s",
                    currentStatus, nextStatus, allowed
            ));
        }
    }
}
