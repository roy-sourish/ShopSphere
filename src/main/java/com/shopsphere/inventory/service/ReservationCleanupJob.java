package com.shopsphere.inventory.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservationCleanupJob {

    private final InventoryReservationService reservationService;

    public ReservationCleanupJob(InventoryReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Scheduled(fixedDelayString = "PT5M")
    public void expireReservations() {
        reservationService.releaseExpiredReservations();
    }
}
