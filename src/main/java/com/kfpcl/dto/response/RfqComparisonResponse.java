package com.kfpcl.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RfqComparisonResponse {

    private RfqResponse rfqDetails;
    private int totalQuotesCount;
    private BigDecimal lowestQuoteAmount;
    private BigDecimal highestQuoteAmount;
    private BigDecimal averageQuoteAmount;
    private List<QuotationResponse> quotations;
}
