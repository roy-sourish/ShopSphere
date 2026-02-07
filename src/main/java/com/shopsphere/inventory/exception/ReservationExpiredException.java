package com.shopsphere.inventory.exception;

public class ReservationExpiredException extends RuntimeException {
    public ReservationExpiredException(Long reservationId) {
        super("Reservation has expired (id=" + reservationId + ")");
    }
}
