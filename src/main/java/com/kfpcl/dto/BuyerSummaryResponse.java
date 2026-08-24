package com.kfpcl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyerSummaryResponse {

    private String buyerId;
    private String name;
    private String email;
    private String companyName;
    private String businessType;
    private String gstNumber;
}
