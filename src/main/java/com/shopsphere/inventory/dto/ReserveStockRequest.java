package com.shopsphere.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ReserveStockRequest {
    @NotNull
    private Long productId;

    @NotNull
    private Long orderId;

    @NotNull
    @Min(1)
    private Integer quantity;

    public Long getProductId() {
        return productId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Integer getQuantity() {
        return quantity;
    }
}
