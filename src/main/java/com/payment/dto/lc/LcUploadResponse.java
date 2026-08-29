package com.payment.dto.lc;

import com.payment.entity.enums.LcStatus;
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
public class LcUploadResponse {

    private Long id;
    private String orderId;
    private String lcNumber;
    private String issuingBank;
    private String advisingBank;
    private BigDecimal lcAmount;
    private LocalDate expiryDate;
    private Integer tenorDays;
    private String documentUrl;
    private LcStatus status;
    private LocalDateTime submittedAt;
    private String message;
}
