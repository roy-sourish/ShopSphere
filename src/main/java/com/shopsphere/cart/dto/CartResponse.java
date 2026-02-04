package com.shopsphere.cart.dto;

import com.shopsphere.cart.domain.Cart;

import java.util.List;

public class CartResponse {
    private Long cartId;
    private String status;
    private List<CartItemResponse> items;

    public CartResponse(Long cartId, String status, List<CartItemResponse> items) {
        this.cartId = cartId;
        this.status = status;
        this.items = items;
    }

    public static CartResponse from(Cart cart) {
        return new CartResponse(
                cart.getId(),
                cart.getStatus().name(),
                cart.getItems().stream()
                        .map(CartItemResponse::from)
                        .toList()
        );
    }

    public Long getCartId(){
        return cartId;
    }

    public String getStatus() {
        return status;
    }

    public List<CartItemResponse> getItems(){
        return items;
    }
}
