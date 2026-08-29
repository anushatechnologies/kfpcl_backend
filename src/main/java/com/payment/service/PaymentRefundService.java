package com.payment.service;

import com.payment.dto.refund.ExecuteRefundRequest;
import com.payment.dto.refund.RefundResponse;

public interface PaymentRefundService {

    RefundResponse processRefund(ExecuteRefundRequest request, String clientIp);
}
