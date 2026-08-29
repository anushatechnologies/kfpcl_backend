package com.payment.service;

import com.payment.dto.history.PaymentHistoryResponse;
import com.payment.entity.enums.PaymentMethod;
import com.payment.entity.enums.PaymentStatus;

import java.time.LocalDateTime;

public interface BuyerPaymentService {

    PaymentHistoryResponse getBuyerPaymentHistory(
            PaymentStatus status,
            PaymentMethod paymentMethod,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size,
            String sortBy,
            String sortDir
    );
}
