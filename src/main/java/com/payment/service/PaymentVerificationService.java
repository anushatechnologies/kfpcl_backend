package com.payment.service;

import com.payment.dto.gateway.VerifyPaymentRequest;
import com.payment.dto.gateway.VerifyPaymentResponse;

public interface PaymentVerificationService {

    VerifyPaymentResponse verifyPayment(VerifyPaymentRequest request, String clientIp);
}
