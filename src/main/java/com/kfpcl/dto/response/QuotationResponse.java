package com.kfpcl.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kfpcl.entity.enums.QuotationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuotationResponse {

    private Long id;
    private Long rfqId;
    private String rfqTitle;
    private Long sellerId;
    private String sellerCompanyName;
    private Boolean sellerIsVerified;
    private Double sellerRating;
    private BigDecimal unitPrice;
    private Integer quantity;
    private String unit;
    private BigDecimal freightCharges;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private Integer deliveryTimelineDays;
    private String paymentTerms;
    private LocalDate validUntil;
    private String notes;
    private QuotationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
