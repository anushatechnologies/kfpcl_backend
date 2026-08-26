package com.kfpcl.service;

import com.kfpcl.dto.request.QuotationSubmitRequest;
import com.kfpcl.dto.response.PageResponse;
import com.kfpcl.dto.response.QuotationResponse;
import com.kfpcl.entity.enums.QuotationStatus;

public interface QuotationService {

    /**
     * Submits or updates a commercial quotation for an open RFQ.
     */
    QuotationResponse submitQuote(String sellerEmail, Long rfqId, QuotationSubmitRequest request);

    /**
     * Retrieves paginated list of quotations submitted by the authenticated seller.
     */
    PageResponse<QuotationResponse> getSellerQuotations(
            String sellerEmail,
            QuotationStatus status,
            int page,
            int size
    );

    /**
     * Retrieves a single quotation details by ID.
     */
    QuotationResponse getQuotationById(Long quoteId);
}
