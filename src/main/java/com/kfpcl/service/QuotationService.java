package com.kfpcl.service;

import com.kfpcl.dto.BuyerOrderResponse;
import com.kfpcl.dto.QuotationCompareResponse;
import com.kfpcl.dto.QuotationResponse;

import java.util.List;

public interface QuotationService {

    List<QuotationResponse> getQuotationsForRfq(String rfqId);

    QuotationCompareResponse compareQuotations(String rfqId);

    QuotationResponse rejectQuotation(String rfqId, String quotationId);

    BuyerOrderResponse acceptQuotation(String rfqId, String quotationId);
}
