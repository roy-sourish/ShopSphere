package com.shopsphere.inventory.service;

import com.shopsphere.inventory.domain.InventoryReservation;
import com.shopsphere.inventory.domain.InventoryStatus;
import com.shopsphere.inventory.repository.InventoryReservationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class InventoryCleanupService {
    private final InventoryReservationRepository reservationRepository;
    private final InventoryService inventoryService;

    public InventoryCleanupService(InventoryReservationRepository reservationRepository,
                                   InventoryService inventoryService) {
        this.reservationRepository = reservationRepository;
        this.inventoryService = inventoryService;
    }

    @Scheduled(fixedDelay = 60_000) // every 60 seconds
    @Transactional
    public void expireOldReservations() {
        // Step 1: Compute cutoff time
        Instant cutoff = Instant.now().minus(15, ChronoUnit.MINUTES);

        // Step 2: Find expired RESERVED reservations
        List<InventoryReservation> expired =
                reservationRepository.findAllByStatusAndCreatedAtBefore(
                        InventoryStatus.RESERVED,
                        cutoff
                );

        for (InventoryReservation reservation : expired) {
            // Release by orderId ensures correct stock restore
            inventoryService.releaseReservation(reservation.getOrderId());
        }

        // Step 3: Group by orderId
        expired.stream()
                .map(InventoryReservation::getOrderId)
                .distinct()
                .forEach(orderId -> {
                    // Step 4: Release order reservations
                    inventoryService.releaseReservation(orderId);
                });
    }

}
