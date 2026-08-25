package com.kfpcl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyerActivityDto {

    private String id;
    private String buyerId;
    private String activityType; // ORDER, RFQ, REVIEW, LOGIN, SUPPORT_TICKET
    private String description;
    private String referenceId;
    private LocalDateTime timestamp;
}
