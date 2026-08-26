package com.kfpcl.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kfpcl.entity.enums.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SellerDashboardStatsResponse {

    // Seller Info & Verification
    private Long sellerId;
    private String companyName;
    private Boolean isVerified;
    private VerificationStatus verificationStatus;
    private Boolean isAllowedToPublish;
    private Double rating;
    private Integer totalReviews;

    // Product Inventory Overview
    private long totalProducts;
    private long activeProducts;
    private long pendingApprovalProducts;
    private long draftProducts;
    private long archivedProducts;
    private long totalCatalogViews;

    // Leads / Inquiries Overview
    private long totalInquiries;
    private long pendingInquiries;
    private long repliedInquiries;

    // RFQ Bidding & Commercial Performance
    private long totalQuotesSubmitted;
    private long activeOpenQuotesCount;
    private long acceptedQuotesCount;
    private long closedOrRejectedQuotesCount;
    private BigDecimal totalOrderRevenueAwarded;
    private Double quotationWinRatePercentage;
}
