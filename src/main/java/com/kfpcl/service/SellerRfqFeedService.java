package com.kfpcl.service;

import com.kfpcl.dto.response.PageResponse;
import com.kfpcl.dto.response.SellerRfqFeedResponse;

public interface SellerRfqFeedService {

    /**
     * Retrieves paginated feed of open RFQ opportunities for verified sellers.
     */
    PageResponse<SellerRfqFeedResponse> getOpenRfqFeed(
            String sellerEmail,
            Long categoryId,
            String keyword,
            int page,
            int size,
            String sortBy,
            String sortDir
    );

    /**
     * Retrieves detailed opportunity view of a specific open RFQ.
     */
    SellerRfqFeedResponse getRfqFeedDetails(String sellerEmail, Long rfqId);
}
