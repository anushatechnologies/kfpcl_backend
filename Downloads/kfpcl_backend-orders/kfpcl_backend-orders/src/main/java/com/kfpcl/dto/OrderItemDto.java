package com.kfpcl.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class OrderItemDto {

    @NotBlank(message = "Product ID is required")
    private String productId;

    private String productName;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    private BigDecimal unitPrice;

    public OrderItemDto() {}

    public OrderItemDto(String productId, String productName, Integer quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public static OrderItemDtoBuilder builder() { return new OrderItemDtoBuilder(); }

    public static class OrderItemDtoBuilder {
        private String productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;

        public OrderItemDtoBuilder productId(String productId) { this.productId = productId; return this; }
        public OrderItemDtoBuilder productName(String productName) { this.productName = productName; return this; }
        public OrderItemDtoBuilder quantity(Integer quantity) { this.quantity = quantity; return this; }
        public OrderItemDtoBuilder unitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; return this; }

        public OrderItemDto build() {
            return new OrderItemDto(productId, productName, quantity, unitPrice);
        }
    }
}
