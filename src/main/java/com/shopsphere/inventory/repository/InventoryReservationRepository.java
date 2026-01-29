package com.shopsphere.inventory.repository;

import com.shopsphere.inventory.domain.InventoryReservation;
import com.shopsphere.inventory.domain.InventoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {
    Optional<InventoryReservation> findByIdempotencyKey(String idempotencyKey);

    Optional<InventoryReservation> findByOrderIdAndProductId(Long orderId, Long productId);

    List<InventoryReservation> findAllByOrderId(Long orderId);

    List<InventoryReservation> findAllByStatusAndCreatedAtBefore(InventoryStatus status, Instant cutoff);
}
