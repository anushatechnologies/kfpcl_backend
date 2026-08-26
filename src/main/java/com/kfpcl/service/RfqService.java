package com.kfpcl.service;

import com.kfpcl.dto.request.RfqCreateRequest;
import com.kfpcl.dto.response.PageResponse;
import com.kfpcl.dto.response.RfqResponse;
import com.kfpcl.entity.enums.RFQStatus;

public interface RfqService {

    /**
     * Creates and broadcasts a custom RFQ requirement from the authenticated buyer.
     */
    RfqResponse createRfq(String buyerEmail, RfqCreateRequest request);

    /**
     * Retrieves paginated list of RFQs created by the authenticated buyer.
     */
    PageResponse<RfqResponse> getBuyerRfqs(String buyerEmail, RFQStatus status, int page, int size);

    /**
     * Retrieves specific RFQ details for the authenticated buyer.
     */
    RfqResponse getBuyerRfqById(String buyerEmail, Long rfqId);

    /**
     * Cancels an open RFQ.
     */
    RfqResponse cancelRfq(String buyerEmail, Long rfqId);
}
