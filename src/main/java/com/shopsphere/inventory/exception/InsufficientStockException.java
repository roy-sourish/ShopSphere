package com.shopsphere.inventory.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(Long productId, int requested, int available) {
        super("Insufficient stock for product " + productId +
                ". Requested = " + requested +
                ", Available = " + available);
    }
}
