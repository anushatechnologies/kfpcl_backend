package com.kfpcl.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuoteAcceptanceResponse {
    private String quotationId;
    private String rfqId;
    private String status;
    private String message;
}
