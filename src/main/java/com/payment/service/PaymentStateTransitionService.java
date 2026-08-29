package com.payment.service;

import com.payment.entity.PaymentTransaction;
import com.payment.entity.enums.PaymentStatus;

public interface PaymentStateTransitionService {

    void validateTransition(PaymentStatus currentStatus, PaymentStatus nextStatus);

    PaymentTransaction transition(PaymentTransaction transaction, PaymentStatus nextStatus, String performedBy, String source, String clientIp, String reason);
}
