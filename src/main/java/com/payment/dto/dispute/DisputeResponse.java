package com.payment.dto.dispute;

import com.payment.entity.enums.DisputeStatus;
import com.payment.entity.enums.PaymentStatus;
import com.payment.entity.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisputeResponse {

    private Long disputeId;
    private String orderId;
    private Long transactionId;
    private String raisedByUserId;
    private UserRole raisedByRole;
    private String reason;
    private DisputeStatus disputeStatus;
    private PaymentStatus paymentStatus;
    private List<String> evidenceUrls;
    private LocalDateTime createdAt;
    private String message;
}
