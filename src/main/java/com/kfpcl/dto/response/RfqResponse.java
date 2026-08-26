package com.kfpcl.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kfpcl.entity.enums.RFQStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RfqResponse {

    private Long id;
    private Long buyerId;
    private String buyerCompanyName;
    private String buyerContactPerson;
    private String buyerEmail;
    private Long categoryId;
    private String categoryName;
    private String categorySlug;
    private String title;
    private String description;
    private Integer quantity;
    private String unit;
    private BigDecimal targetUnitPrice;
    private String deliveryLocation;
    private LocalDate expectedDeliveryDate;
    private String paymentTerms;
    private RFQStatus status;
    private Map<String, Object> specifications;
    private Long totalQuotesCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
