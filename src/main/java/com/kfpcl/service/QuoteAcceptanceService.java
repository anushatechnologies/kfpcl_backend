package com.kfpcl.service;

import com.kfpcl.dto.response.QuoteAcceptanceResponse;

public interface QuoteAcceptanceService {

    /**
     * Transactionally accepts a quotation for an RFQ, closes competing bids,
     * marks RFQ as AWARDED, and emits the OrderCreationEvent.
     */
    QuoteAcceptanceResponse acceptQuote(String buyerEmail, Long rfqId, Long quoteId);
}
