package com.kfpcl.event;

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
public class OrderCreationEvent {

    private String eventId;
    private Long rfqId;
    private String rfqTitle;
    private Long quotationId;
    private Long buyerId;
    private String buyerEmail;
    private String buyerCompanyName;
    private Long sellerId;
    private String sellerEmail;
    private String sellerCompanyName;
    private Integer quantity;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal freightCharges;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String deliveryLocation;
    private String paymentTerms;
    private Integer deliveryTimelineDays;
    @Builder.Default
    private LocalDateTime eventTimestamp = LocalDateTime.now();
}
