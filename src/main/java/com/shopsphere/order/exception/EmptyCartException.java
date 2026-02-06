package com.shopsphere.order.exception;

public class EmptyCartException extends RuntimeException {
    public EmptyCartException() {
        super("Cannot create order from empty cart");
    }
}
