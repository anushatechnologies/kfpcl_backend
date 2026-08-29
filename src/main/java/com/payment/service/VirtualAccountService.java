package com.payment.service;

import com.payment.dto.escrow.VirtualAccountResponse;

public interface VirtualAccountService {

    VirtualAccountResponse getVirtualAccountDetails(String orderId);
}
