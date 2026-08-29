package com.payment.service;

import com.payment.dto.dispute.DisputeResponse;
import com.payment.dto.dispute.RaiseDisputeRequest;

public interface PaymentDisputeService {

    DisputeResponse raiseDispute(RaiseDisputeRequest request, String clientIp);
}
