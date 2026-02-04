package com.shopsphere.cart.dto;

import com.shopsphere.cart.domain.CartItem;

public class CartItemResponse {
    private Long productId;
    private String productName;
    private Integer quantity;

    public CartItemResponse(Long productId, String productName, Integer quantity){
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
    }

    public static CartItemResponse from(CartItem item){
        return new CartItemResponse(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity()
        );
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Integer getQuantity() {
        return quantity;
    }
}
