package com.payment.dto.gateway;

import com.payment.entity.enums.PaymentGatewayType;
import com.payment.entity.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGatewayOrderRequest {

    @NotBlank(message = "Order ID is mandatory")
    private String orderId;

    @Builder.Default
    private PaymentGatewayType gateway = PaymentGatewayType.RAZORPAY;

    private PaymentMethod paymentMethod; // Optional, defaults to CARD/UPI
}
