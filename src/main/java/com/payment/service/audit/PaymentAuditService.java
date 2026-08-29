package com.payment.service.audit;

import com.payment.entity.PaymentAuditLog;
import com.payment.repository.PaymentAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentAuditService {

    private final PaymentAuditLogRepository auditLogRepository;

    public void logAction(Long transactionId, String orderId, String action,
                          String previousStatus, String newStatus,
                          String performedBy, String source, String ipAddress, String metadata) {
        try {
            PaymentAuditLog auditLog = PaymentAuditLog.builder()
                    .transactionId(transactionId)
                    .orderId(orderId)
                    .action(action)
                    .previousStatus(previousStatus)
                    .newStatus(newStatus)
                    .performedBy(performedBy)
                    .source(source)
                    .ipAddress(ipAddress)
                    .metadata(metadata)
                    .build();
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to write payment audit log for order {}: ", orderId, e);
        }
    }
}
