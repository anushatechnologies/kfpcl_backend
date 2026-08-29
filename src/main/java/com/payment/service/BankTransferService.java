package com.payment.service;

import com.payment.dto.bank.BankTransferConfirmRequest;
import com.payment.dto.bank.BankTransferConfirmResponse;

public interface BankTransferService {

    BankTransferConfirmResponse confirmBankTransfer(BankTransferConfirmRequest request, String clientIp);
}
