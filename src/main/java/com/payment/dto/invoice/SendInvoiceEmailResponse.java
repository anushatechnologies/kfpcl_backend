package com.payment.dto.invoice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendInvoiceEmailResponse {

    private String orderId;
    private String invoiceNumber;
    private String recipientEmail;
    private Boolean emailSent;
    private LocalDateTime sentAt;
    private String message;
}
