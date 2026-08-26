package com.kfpcl.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kfpcl.entity.enums.InquiryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InquiryResponse {

    private Long id;
    private Long buyerId;
    private String buyerCompanyName;
    private String buyerContactPerson;
    private String buyerEmail;
    private Long sellerId;
    private String sellerCompanyName;
    private Long productId;
    private String productName;
    private String productSlug;
    private BigDecimal productBasePrice;
    private String productUnit;
    private String productPrimaryImageUrl;
    private String subject;
    private String message;
    private Integer quantity;
    private BigDecimal targetPrice;
    private InquiryStatus status;
    private String sellerReply;
    private LocalDateTime repliedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
