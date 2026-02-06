package com.shopsphere.order.dto;

import com.shopsphere.order.domain.OrderItem;

import java.math.BigDecimal;

public class OrderItemResponse {
    private Long productId;
    private String productName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal lineTotal;

    public OrderItemResponse(Long productId, String productName,
                             BigDecimal unitPrice, Integer quantity, BigDecimal lineTotal) {
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
    }

    public static OrderItemResponse from(OrderItem item){
        return new OrderItemResponse(
                item.getProductId(),
                item.getProductNameSnapshot(),
                item.getUnitPriceSnapshot(),
                item.getQuantity(),
                item.getLineTotal()
        );
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }
}
