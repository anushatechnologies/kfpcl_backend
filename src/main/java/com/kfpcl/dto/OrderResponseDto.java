package com.kfpcl.dto;

import com.kfpcl.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponseDto {

    private Long id;
    private String orderNumber;
    private String buyerId;
    private String sellerId;
    private OrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal shippingCost;
    private BigDecimal grandTotal;
    private String shippingAddress;
    private String paymentMethod;
    private String paymentStatus;
    private String trackingNo;
    private String shippingCarrier;
    private String idempotencyKey;
    private List<OrderItemDto> items;
    private List<OrderAuditLogDto> auditTimeline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OrderResponseDto() {}

    public OrderResponseDto(Long id, String orderNumber, String buyerId, String sellerId, OrderStatus status,
                            BigDecimal subtotal, BigDecimal tax, BigDecimal shippingCost, BigDecimal grandTotal,
                            String shippingAddress, String paymentMethod, String paymentStatus, String trackingNo,
                            String shippingCarrier, String idempotencyKey, List<OrderItemDto> items,
                            List<OrderAuditLogDto> auditTimeline, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.status = status;
        this.subtotal = subtotal;
        this.tax = tax;
        this.shippingCost = shippingCost;
        this.grandTotal = grandTotal;
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.trackingNo = trackingNo;
        this.shippingCarrier = shippingCarrier;
        this.idempotencyKey = idempotencyKey;
        this.items = items;
        this.auditTimeline = auditTimeline;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public String getBuyerId() { return buyerId; }
    public void setBuyerId(String buyerId) { this.buyerId = buyerId; }

    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getTax() { return tax; }
    public void setTax(BigDecimal tax) { this.tax = tax; }

    public BigDecimal getShippingCost() { return shippingCost; }
    public void setShippingCost(BigDecimal shippingCost) { this.shippingCost = shippingCost; }

    public BigDecimal getGrandTotal() { return grandTotal; }
    public void setGrandTotal(BigDecimal grandTotal) { this.grandTotal = grandTotal; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getTrackingNo() { return trackingNo; }
    public void setTrackingNo(String trackingNo) { this.trackingNo = trackingNo; }

    public String getShippingCarrier() { return shippingCarrier; }
    public void setShippingCarrier(String shippingCarrier) { this.shippingCarrier = shippingCarrier; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public List<OrderItemDto> getItems() { return items; }
    public void setItems(List<OrderItemDto> items) { this.items = items; }

    public List<OrderAuditLogDto> getAuditTimeline() { return auditTimeline; }
    public void setAuditTimeline(List<OrderAuditLogDto> auditTimeline) { this.auditTimeline = auditTimeline; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static OrderResponseDtoBuilder builder() { return new OrderResponseDtoBuilder(); }

    public static class OrderResponseDtoBuilder {
        private Long id;
        private String orderNumber;
        private String buyerId;
        private String sellerId;
        private OrderStatus status;
        private BigDecimal subtotal;
        private BigDecimal tax;
        private BigDecimal shippingCost;
        private BigDecimal grandTotal;
        private String shippingAddress;
        private String paymentMethod;
        private String paymentStatus;
        private String trackingNo;
        private String shippingCarrier;
        private String idempotencyKey;
        private List<OrderItemDto> items;
        private List<OrderAuditLogDto> auditTimeline;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public OrderResponseDtoBuilder id(Long id) { this.id = id; return this; }
        public OrderResponseDtoBuilder orderNumber(String orderNumber) { this.orderNumber = orderNumber; return this; }
        public OrderResponseDtoBuilder buyerId(String buyerId) { this.buyerId = buyerId; return this; }
        public OrderResponseDtoBuilder sellerId(String sellerId) { this.sellerId = sellerId; return this; }
        public OrderResponseDtoBuilder status(OrderStatus status) { this.status = status; return this; }
        public OrderResponseDtoBuilder subtotal(BigDecimal subtotal) { this.subtotal = subtotal; return this; }
        public OrderResponseDtoBuilder tax(BigDecimal tax) { this.tax = tax; return this; }
        public OrderResponseDtoBuilder shippingCost(BigDecimal shippingCost) { this.shippingCost = shippingCost; return this; }
        public OrderResponseDtoBuilder grandTotal(BigDecimal grandTotal) { this.grandTotal = grandTotal; return this; }
        public OrderResponseDtoBuilder shippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; return this; }
        public OrderResponseDtoBuilder paymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; return this; }
        public OrderResponseDtoBuilder paymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; return this; }
        public OrderResponseDtoBuilder trackingNo(String trackingNo) { this.trackingNo = trackingNo; return this; }
        public OrderResponseDtoBuilder shippingCarrier(String shippingCarrier) { this.shippingCarrier = shippingCarrier; return this; }
        public OrderResponseDtoBuilder idempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; return this; }
        public OrderResponseDtoBuilder items(List<OrderItemDto> items) { this.items = items; return this; }
        public OrderResponseDtoBuilder auditTimeline(List<OrderAuditLogDto> auditTimeline) { this.auditTimeline = auditTimeline; return this; }
        public OrderResponseDtoBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public OrderResponseDtoBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public OrderResponseDto build() {
            return new OrderResponseDto(id, orderNumber, buyerId, sellerId, status, subtotal, tax, shippingCost, grandTotal,
                    shippingAddress, paymentMethod, paymentStatus, trackingNo, shippingCarrier, idempotencyKey, items, auditTimeline, createdAt, updatedAt);
        }
    }
}
