package com.shopsphere.inventory.exception;

public class ReservationNotFoundException extends RuntimeException {
    public ReservationNotFoundException(Long orderId) {
        super("No reservation found for order: " + orderId);
    }
}
