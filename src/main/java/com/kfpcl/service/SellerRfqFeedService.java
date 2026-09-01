package com.kfpcl.service;

import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.dto.QuotationResponseDto;
import com.kfpcl.dto.RfqResponseDto;
import com.kfpcl.dto.request.QuotationSubmitRequest;

public interface SellerRfqFeedService {
    PageResponseDto<RfqResponseDto> getOpenRfqs(int page, int size);
    QuotationResponseDto submitQuote(String sellerId, String rfqId, QuotationSubmitRequest request);
    PageResponseDto<QuotationResponseDto> getMyQuotes(String sellerId, int page, int size);
}
