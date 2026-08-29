package com.payment.dto.payout;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerPayoutSummaryDto {

    private BigDecimal totalGross;
    private BigDecimal totalPlatformFees;
    private BigDecimal totalTaxDeductions;
    private BigDecimal totalNetPayouts;
    private long totalPayoutsCount;
    private long pendingCount;
    private long completedCount;
}
