package com.kfpcl.service;

import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.dto.RfqResponseDto;
import com.kfpcl.dto.request.RfqCreateRequest;
import com.kfpcl.dto.response.QuoteAcceptanceResponse;

public interface BuyerRfqService {
    RfqResponseDto createRfq(String buyerId, RfqCreateRequest request);
    PageResponseDto<RfqResponseDto> getBuyerRfqs(String buyerId, int page, int size);
    QuoteAcceptanceResponse acceptQuote(String buyerId, String rfqId, String quoteId);
}
