package com.kfpcl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyerHomeResponse {

    private BuyerSummaryResponse buyer;
    private long wishlistCount;
    private long activeRfqsCount;
    private long totalOrdersCount;
    private List<ProductResponse> featuredProducts;
    private List<ProductResponse> recommendations;
    private List<ProductResponse> recentlyViewed;
    private List<RfqSummaryResponse> activeRfqs;
    private List<BuyerOrderSummaryResponse> recentOrders;
}
