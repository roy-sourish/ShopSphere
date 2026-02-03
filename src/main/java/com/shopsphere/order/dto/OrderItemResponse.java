package com.shopsphere.order.dto;

import com.shopsphere.order.domain.OrderItem;

import java.math.BigDecimal;

public class OrderItemResponse {
    private Long productId;
    private String productName;
    private BigDecimal unitPrice;
    private Integer quantity;

    public static OrderItemResponse from(OrderItem item) {
        OrderItemResponse dto = new OrderItemResponse();
        dto.productId = item.getProductId();
        dto.productName = item.getProductNameSnapshot();
        dto.unitPrice = item.getUnitPriceSnapshot();
        dto.quantity = item.getQuantity();
        return dto;
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
}
