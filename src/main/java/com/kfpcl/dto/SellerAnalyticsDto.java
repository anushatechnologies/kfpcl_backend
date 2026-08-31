package com.kfpcl.dto;

import java.math.BigDecimal;

public class SellerAnalyticsDto {
    private BigDecimal totalRevenue;
    private long totalOrders;
    private long completedOrders;
    private long pendingOrders;
    private BigDecimal averageOrderValue;
    private long repeatBuyersCount;

    public SellerAnalyticsDto() {}

    public SellerAnalyticsDto(BigDecimal totalRevenue, long totalOrders, long completedOrders,
                              long pendingOrders, BigDecimal averageOrderValue, long repeatBuyersCount) {
        this.totalRevenue = totalRevenue;
        this.totalOrders = totalOrders;
        this.completedOrders = completedOrders;
        this.pendingOrders = pendingOrders;
        this.averageOrderValue = averageOrderValue;
        this.repeatBuyersCount = repeatBuyersCount;
    }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }

    public long getCompletedOrders() { return completedOrders; }
    public void setCompletedOrders(long completedOrders) { this.completedOrders = completedOrders; }

    public long getPendingOrders() { return pendingOrders; }
    public void setPendingOrders(long pendingOrders) { this.pendingOrders = pendingOrders; }

    public BigDecimal getAverageOrderValue() { return averageOrderValue; }
    public void setAverageOrderValue(BigDecimal averageOrderValue) { this.averageOrderValue = averageOrderValue; }

    public long getRepeatBuyersCount() { return repeatBuyersCount; }
    public void setRepeatBuyersCount(long repeatBuyersCount) { this.repeatBuyersCount = repeatBuyersCount; }

    public static SellerAnalyticsDtoBuilder builder() { return new SellerAnalyticsDtoBuilder(); }

    public static class SellerAnalyticsDtoBuilder {
        private BigDecimal totalRevenue;
        private long totalOrders;
        private long completedOrders;
        private long pendingOrders;
        private BigDecimal averageOrderValue;
        private long repeatBuyersCount;

        public SellerAnalyticsDtoBuilder totalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; return this; }
        public SellerAnalyticsDtoBuilder totalOrders(long totalOrders) { this.totalOrders = totalOrders; return this; }
        public SellerAnalyticsDtoBuilder completedOrders(long completedOrders) { this.completedOrders = completedOrders; return this; }
        public SellerAnalyticsDtoBuilder pendingOrders(long pendingOrders) { this.pendingOrders = pendingOrders; return this; }
        public SellerAnalyticsDtoBuilder averageOrderValue(BigDecimal averageOrderValue) { this.averageOrderValue = averageOrderValue; return this; }
        public SellerAnalyticsDtoBuilder repeatBuyersCount(long repeatBuyersCount) { this.repeatBuyersCount = repeatBuyersCount; return this; }

        public SellerAnalyticsDto build() {
            return new SellerAnalyticsDto(totalRevenue, totalOrders, completedOrders, pendingOrders, averageOrderValue, repeatBuyersCount);
        }
    }
}
