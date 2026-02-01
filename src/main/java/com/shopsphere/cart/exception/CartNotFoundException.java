package com.shopsphere.cart.exception;

public class CartNotFoundException extends RuntimeException {
    public CartNotFoundException(Long userId) {
        super("Active cart not found for user: " + userId);
    }
}
