package com.kfpcl.service;

import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.dto.QuotationResponseDto;
import com.kfpcl.dto.RfqResponseDto;

import java.util.List;

public interface AdminRfqService {

    PageResponseDto<RfqResponseDto> getRfqs(String search, String status, int page, int size, String sortBy, String sortDir);

    RfqResponseDto getRfqById(String rfqId);

    List<QuotationResponseDto> getQuotationsForRfq(String rfqId);
}
