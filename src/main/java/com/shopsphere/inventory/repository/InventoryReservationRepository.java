package com.shopsphere.inventory.repository;

import com.shopsphere.inventory.domain.InventoryReservation;
import com.shopsphere.inventory.domain.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {
    List<InventoryReservation> findByCartId(Long cartId);

    List<InventoryReservation> findByOrderId(Long orderId);

    List<InventoryReservation> findByOrderIdAndStatus(Long orderId, ReservationStatus status);

    List<InventoryReservation> findByCartIdAndStatus(Long cartId, ReservationStatus status);

    List<InventoryReservation> findByStatusAndExpiresAtBefore(ReservationStatus status, Instant cutoff);
}
