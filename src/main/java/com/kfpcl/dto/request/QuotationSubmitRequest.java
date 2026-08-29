package com.kfpcl.dto.request;

import lombok.Data;

@Data
public class QuotationSubmitRequest {
    private Double unitPrice;
    private Double freight;
    private Double totalPrice;
    private Integer deliveryDays;
    private String timeline;
    private String paymentTerms;
    private String warranty;
    private String notes;
}
