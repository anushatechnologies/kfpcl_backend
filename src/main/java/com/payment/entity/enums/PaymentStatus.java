package com.payment.entity.enums;

public enum PaymentStatus {
    PENDING_PAYMENT,
    PAYMENT_PROCESSING,
    ESCROW_LOCKED,
    DISPATCH_ALLOWED,
    FUNDS_RELEASED,
    DISPUTED,
    REFUNDED,
    FAILED,
    CANCELLED
}
