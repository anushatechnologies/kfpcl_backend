package com.kfpcl.dto.request;
import lombok.Data;

@Data
public class InquiryRequest {
    private String sellerId;
    private String productId;
    private String message;
}
