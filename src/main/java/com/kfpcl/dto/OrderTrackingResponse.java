package com.kfpcl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderTrackingResponse {

    private String orderId;
    private String status;
    private String courierPartner;
    private String trackingNumber;
    private String shippingAddress;
    private LocalDate estimatedDelivery;
    private LocalDateTime deliveredAt;
    private LocalDateTime orderDate;
}
