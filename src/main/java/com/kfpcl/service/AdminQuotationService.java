package com.kfpcl.service;

import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.dto.QuotationResponseDto;

public interface AdminQuotationService {

    PageResponseDto<QuotationResponseDto> getQuotations(String status, int page, int size, String sortBy, String sortDir);

    QuotationResponseDto getQuotationById(String quotationId);

    QuotationResponseDto approveQuotation(String quotationId);

    QuotationResponseDto rejectQuotation(String quotationId);
}
