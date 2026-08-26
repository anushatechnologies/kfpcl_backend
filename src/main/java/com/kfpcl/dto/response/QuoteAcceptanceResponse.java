package com.kfpcl.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kfpcl.entity.enums.RFQStatus;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuoteAcceptanceResponse {

    private Long rfqId;
    private String rfqTitle;
    private Long acceptedQuoteId;
    private Long winningSellerId;
    private String winningSellerCompanyName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private String unit;
    private BigDecimal freightCharges;
    private BigDecimal taxAmount;
    private BigDecimal totalOrderAmount;
    private Integer deliveryTimelineDays;
    private String paymentTerms;
    private RFQStatus rfqStatus;
    private int closedCompetingQuotesCount;
    private String orderTrackingReference;
    private LocalDateTime acceptedAt;
    private String message;
}
