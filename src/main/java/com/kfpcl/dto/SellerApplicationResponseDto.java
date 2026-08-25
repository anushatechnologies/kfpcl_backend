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
public class SellerApplicationResponseDto {

    private String id;
    private String userId;
    private String userName;
    private String userEmail;
    private String businessName;
    private String businessType;
    private String gstin;
    private String panNumber;
    private String address;
    private String documents;
    private String status;
    private String rejectionReason;
    private LocalDateTime verifiedAt;
    private String verifiedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
