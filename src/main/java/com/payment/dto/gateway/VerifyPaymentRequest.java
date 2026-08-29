package com.payment.dto.gateway;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyPaymentRequest {

    @NotBlank(message = "Order ID is mandatory")
    private String orderId;

    @NotBlank(message = "Gateway Order ID is mandatory")
    private String gatewayOrderId;

    @NotBlank(message = "Payment ID is mandatory")
    private String paymentId;

    @NotBlank(message = "Payment signature is mandatory")
    private String signature;
}
