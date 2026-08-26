package com.kfpcl.dto;

import com.kfpcl.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public class OrderStatusUpdateDto {

    @NotNull(message = "Status is required")
    private OrderStatus status;

    private String trackingNo;

    private String shippingCarrier;

    public OrderStatusUpdateDto() {}

    public OrderStatusUpdateDto(OrderStatus status, String trackingNo, String shippingCarrier) {
        this.status = status;
        this.trackingNo = trackingNo;
        this.shippingCarrier = shippingCarrier;
    }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public String getTrackingNo() { return trackingNo; }
    public void setTrackingNo(String trackingNo) { this.trackingNo = trackingNo; }

    public String getShippingCarrier() { return shippingCarrier; }
    public void setShippingCarrier(String shippingCarrier) { this.shippingCarrier = shippingCarrier; }

    public static OrderStatusUpdateDtoBuilder builder() { return new OrderStatusUpdateDtoBuilder(); }

    public static class OrderStatusUpdateDtoBuilder {
        private OrderStatus status;
        private String trackingNo;
        private String shippingCarrier;

        public OrderStatusUpdateDtoBuilder status(OrderStatus status) { this.status = status; return this; }
        public OrderStatusUpdateDtoBuilder trackingNo(String trackingNo) { this.trackingNo = trackingNo; return this; }
        public OrderStatusUpdateDtoBuilder shippingCarrier(String shippingCarrier) { this.shippingCarrier = shippingCarrier; return this; }

        public OrderStatusUpdateDto build() {
            return new OrderStatusUpdateDto(status, trackingNo, shippingCarrier);
        }
    }
}
