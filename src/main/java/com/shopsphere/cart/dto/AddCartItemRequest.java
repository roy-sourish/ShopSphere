package com.shopsphere.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class AddCartItemRequest {
    @NotNull(message = "ProductId is required")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    public Long getProductId(){
        return productId;
    }

    public Integer getQuantity(){
        return quantity;
    }
}
