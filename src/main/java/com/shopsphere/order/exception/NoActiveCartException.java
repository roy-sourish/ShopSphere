package com.shopsphere.order.exception;

public class NoActiveCartException extends RuntimeException {
    public NoActiveCartException(Long userId) {
        super("No active cart found for the userId = " + userId);
    }
}
