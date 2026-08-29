package com.payment.dto.payout;

import com.payment.entity.enums.PayoutStatus;
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
public class SellerPayoutItemDto {

    private Long payoutId;
    private String orderId;
    private String sellerId;
    private Long escrowAccountId;
    private BigDecimal grossAmount;
    private BigDecimal platformFee;      // 2% platform commission
    private BigDecimal taxDeduction;    // 1% TDS deduction (Section 194-O)
    private BigDecimal netAmount;       // 97% payable to seller
    private PayoutStatus status;
    private String bankReference;
    private LocalDateTime initiatedAt;
    private LocalDateTime completedAt;
}
