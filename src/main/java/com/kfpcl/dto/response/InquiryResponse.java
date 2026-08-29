package com.kfpcl.dto.response;

import com.kfpcl.entity.Inquiry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryResponse {
    private String id;
    private String buyerId;
    private String sellerId;
    private String sellerName;
    private String productId;
    private String productName;
    private String message;
    private Inquiry.Status status;
    private LocalDateTime createdAt;
}
