package com.payment.service;

import com.payment.dto.payout.SellerPayoutResponse;
import com.payment.entity.enums.PayoutStatus;

import java.time.LocalDateTime;

public interface SellerPayoutService {

    SellerPayoutResponse getSellerPayouts(
            PayoutStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size,
            String sortBy,
            String sortDir
    );
}
