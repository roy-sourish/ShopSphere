package com.shopsphere.order.exception;

public class NoCheckedOutCartException extends RuntimeException {
    public NoCheckedOutCartException(Long userId) {
        super("No checked out cart found for user (userId=" + userId + ")");
    }
}
