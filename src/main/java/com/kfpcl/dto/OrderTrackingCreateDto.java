package com.kfpcl.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderTrackingCreateDto {

    private String carrier;

    private String trackingNumber;

    @NotBlank(message = "Tracking status is required")
    private String status;

    private String location;

    private String remarks;

    private LocalDateTime estimatedDelivery;
}
