package com.shopsphere.order.exception;

public class CartAlreadyCheckedOutException extends RuntimeException {
    public CartAlreadyCheckedOutException() {
        super("Cart has already been checked out");
    }
}
