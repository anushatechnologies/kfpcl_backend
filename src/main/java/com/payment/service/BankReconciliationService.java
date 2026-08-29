package com.payment.service;

import com.payment.dto.webhook.BankReconciliationWebhookRequest;
import com.payment.dto.webhook.BankReconciliationWebhookResponse;

public interface BankReconciliationService {

    BankReconciliationWebhookResponse processReconciliation(
            BankReconciliationWebhookRequest request, String webhookSecret, String clientIp);
}
