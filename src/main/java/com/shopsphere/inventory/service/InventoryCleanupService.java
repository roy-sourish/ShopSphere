package com.shopsphere.inventory.service;

import com.shopsphere.inventory.domain.InventoryReservation;
import com.shopsphere.inventory.domain.InventoryStatus;
import com.shopsphere.inventory.repository.InventoryReservationRepository;
import com.shopsphere.order.service.OrderService;
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
    private final OrderService orderService;

    public InventoryCleanupService(InventoryReservationRepository reservationRepository,
                                   InventoryService inventoryService, OrderService orderService) {
        this.reservationRepository = reservationRepository;
        this.inventoryService = inventoryService;
        this.orderService = orderService;
    }

    /**
     * Runs every minute:
     * - Finds expired RESERVED reservations
     * - Cancels the associated PENDING orders
     * - Releases stock safely
     */
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

        if(expired.isEmpty()){
            return;
        }
        // Step 3: Group by orderId
        // Step 4: Release order reservations
        expired.stream()
                .map(InventoryReservation::getOrderId)
                .distinct()
                .forEach(orderService::cancelExpiredOrder);
    }

}
