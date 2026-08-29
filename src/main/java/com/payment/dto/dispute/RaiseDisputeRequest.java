package com.payment.dto.dispute;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RaiseDisputeRequest {

    @NotBlank(message = "Order ID is mandatory")
    private String orderId;

    @NotBlank(message = "Dispute reason is mandatory")
    @Size(min = 10, max = 1000, message = "Reason must be between 10 and 1000 characters")
    private String reason;

    private List<String> evidenceUrls;
}
