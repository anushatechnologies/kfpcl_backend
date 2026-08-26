package com.kfpcl.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public class OrderCreateDto {

    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    private List<OrderItemDto> items;

    @NotNull(message = "Shipping address is required")
    private String shippingAddress;

    private String paymentMethod;

    private String sellerId;

    private BigDecimal subtotal;

    private BigDecimal tax;

    private BigDecimal shippingCost;

    private BigDecimal grandTotal;

    public OrderCreateDto() {}

    public OrderCreateDto(List<OrderItemDto> items, String shippingAddress, String paymentMethod, String sellerId,
                          BigDecimal subtotal, BigDecimal tax, BigDecimal shippingCost, BigDecimal grandTotal) {
        this.items = items;
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
        this.sellerId = sellerId;
        this.subtotal = subtotal;
        this.tax = tax;
        this.shippingCost = shippingCost;
        this.grandTotal = grandTotal;
    }

    public List<OrderItemDto> getItems() { return items; }
    public void setItems(List<OrderItemDto> items) { this.items = items; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getTax() { return tax; }
    public void setTax(BigDecimal tax) { this.tax = tax; }

    public BigDecimal getShippingCost() { return shippingCost; }
    public void setShippingCost(BigDecimal shippingCost) { this.shippingCost = shippingCost; }

    public BigDecimal getGrandTotal() { return grandTotal; }
    public void setGrandTotal(BigDecimal grandTotal) { this.grandTotal = grandTotal; }

    public static OrderCreateDtoBuilder builder() { return new OrderCreateDtoBuilder(); }

    public static class OrderCreateDtoBuilder {
        private List<OrderItemDto> items;
        private String shippingAddress;
        private String paymentMethod;
        private String sellerId;
        private BigDecimal subtotal;
        private BigDecimal tax;
        private BigDecimal shippingCost;
        private BigDecimal grandTotal;

        public OrderCreateDtoBuilder items(List<OrderItemDto> items) { this.items = items; return this; }
        public OrderCreateDtoBuilder shippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; return this; }
        public OrderCreateDtoBuilder paymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; return this; }
        public OrderCreateDtoBuilder sellerId(String sellerId) { this.sellerId = sellerId; return this; }
        public OrderCreateDtoBuilder subtotal(BigDecimal subtotal) { this.subtotal = subtotal; return this; }
        public OrderCreateDtoBuilder tax(BigDecimal tax) { this.tax = tax; return this; }
        public OrderCreateDtoBuilder shippingCost(BigDecimal shippingCost) { this.shippingCost = shippingCost; return this; }
        public OrderCreateDtoBuilder grandTotal(BigDecimal grandTotal) { this.grandTotal = grandTotal; return this; }

        public OrderCreateDto build() {
            return new OrderCreateDto(items, shippingAddress, paymentMethod, sellerId, subtotal, tax, shippingCost, grandTotal);
        }
    }
}
