package com.kfpcl.service;

import com.kfpcl.dto.response.RfqComparisonResponse;

public interface BuyerRfqComparisonService {

    /**
     * Retrieves all quotations submitted for a specific buyer RFQ with side-by-side comparison metrics.
     */
    RfqComparisonResponse getRfqQuotationsComparison(
            String buyerEmail,
            Long rfqId,
            String sortBy,
            String sortDir
    );
}
