package com.shopsphere.inventory.exception;

public class ReservationNotFoundException extends RuntimeException {
    public ReservationNotFoundException(String referenceType, Long referenceId) {
        super("No reservations found for " + referenceType + " (id=" + referenceId + ")");
    }
}
