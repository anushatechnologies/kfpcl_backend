package com.kfpcl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotationCompareResponse {

    private String rfqId;
    private String productTitle;
    private Integer rfqQuantity;
    private BigDecimal targetPrice;
    private BigDecimal lowestQuotedPrice;
    private Integer fastestLeadTimeDays;
    private List<QuotationResponse> quotations;
}
