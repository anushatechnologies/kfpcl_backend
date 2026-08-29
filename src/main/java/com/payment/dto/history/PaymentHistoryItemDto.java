package com.payment.dto.history;

import com.payment.entity.enums.PaymentGatewayType;
import com.payment.entity.enums.PaymentMethod;
import com.payment.entity.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistoryItemDto {

    private String transactionReference;
    private String orderId;
    private String sellerId;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod paymentMethod;
    private PaymentGatewayType gateway;
    private PaymentStatus status;
    private String utrNumber;
    private String invoiceNumber;
    private String invoiceDownloadUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
