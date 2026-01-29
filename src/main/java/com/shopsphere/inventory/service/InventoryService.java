package com.shopsphere.inventory.service;

import com.shopsphere.common.exception.OptimisticConflictException;
import com.shopsphere.inventory.domain.InventoryReservation;
import com.shopsphere.inventory.domain.InventoryStatus;
import com.shopsphere.inventory.exception.InsufficientStockException;
import com.shopsphere.inventory.exception.InvalidReservationStateException;
import com.shopsphere.inventory.exception.ReservationNotFoundException;
import com.shopsphere.inventory.repository.InventoryReservationRepository;
import com.shopsphere.product.domain.Product;
import com.shopsphere.product.exception.ProductNotFoundException;
import com.shopsphere.product.repository.ProductRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {
    private final ProductRepository productRepository;
    private final InventoryReservationRepository reservationRepository;

    public InventoryService(ProductRepository productRepository,
                            InventoryReservationRepository reservationRepository) {
        this.productRepository = productRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public InventoryReservation reserveStock(
            Long productId,
            Long orderId,
            int quantity,
            String idempotencyKey
    ) {
        // Step 0: Idempotency replay - never touch stock until the request is new
        Optional<InventoryReservation> existing =
                reservationRepository.findByIdempotencyKey(idempotencyKey);

        if (existing.isPresent()) {
            InventoryReservation existingReservation = existing.get();

            if (!existingReservation.getProductId().equals(productId) ||
                    !existingReservation.getOrderId().equals(orderId) ||
                    existingReservation.getQuantity() != quantity
            ) {
                throw new IllegalArgumentException(
                        "Idempotency key reuse with different payload in not allowed"
                );
            }

            return existingReservation;
        }

        // Step 1: Retry Loop for Concurrency
        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                // Step 2: Load Product
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new ProductNotFoundException(productId));

                // Step 3: Decrease stock safely
                try {
                    product.decreaseStock(quantity);
                } catch (IllegalStateException ex) {
                    throw new InsufficientStockException(
                            productId,
                            quantity,
                            product.getStockQuantity()
                    );
                }

                // Step 4: Create Reservation - default to RESERVED
                InventoryReservation reservation = new InventoryReservation(
                        productId,
                        orderId,
                        quantity,
                        idempotencyKey
                );

                reservationRepository.save(reservation);

                // Step 5: Flush immediately
                productRepository.flush();
                reservationRepository.flush();

                return reservation;

            } catch (ObjectOptimisticLockingFailureException ex) {
                if (attempt == maxRetries) {
                    throw new OptimisticConflictException("Product", productId);
                }
            } catch (DataIntegrityViolationException ex) {
                // Another request already created reservation
                // return existing reservation - same orderID and product with different key
                return reservationRepository.findByIdempotencyKey(idempotencyKey)
                        .orElseGet(() ->
                                reservationRepository.findByOrderIdAndProductId(orderId, productId)
                                        .orElseThrow(() -> ex)
                        );
            }
        }
        throw new IllegalStateException("Reservation retry loop failed unexpectedly");
    }

    @Transactional
    public void confirmReservation(Long orderId){
        // Step 1: Load all reservations for order
        List<InventoryReservation> reservations = reservationRepository.findAllByOrderId(orderId);

        // Step 2: Check if reservation exists
        if(reservations.isEmpty()){
            throw new ReservationNotFoundException(orderId);
        }

        // Step 3: Go through each reservation one-by-one
        for (InventoryReservation reservation : reservations){
            // Step 4: Retry-safe - already purchased, skip it
            if(reservation.getStatus() == InventoryStatus.PURCHASED){
                continue;
            }

            // Step 5: Illegal - cannot confirm released
            if(reservation.getStatus() == InventoryStatus.RELEASED){
                throw new InvalidReservationStateException(
                        "Cannot confirm RELEASED reservation for order " + orderId
                );
            }

            // Step 6: Transition RESERVED -> PURCHASED
            reservation.markPurchased();
        }

        // Step 7: Flush - detect optimistic lock early
        reservationRepository.flush();
    }

    @Transactional
    public void releaseReservation(Long orderId){
        // Step 1: Load all reservations for order
        List<InventoryReservation> reservations = reservationRepository.findAllByOrderId(orderId);

        // Step 2: Check if reservation exists
        // RELEASE triggered by expiry job, cancel retries
        // If already released, no work needed
        if(reservations.isEmpty()){
            return;
        }

        // Step 3: Go through each reservation one-by-one
        for(InventoryReservation reservation : reservations){
            // Step 4: Retry-safe - already released, skip it
            if(reservation.getStatus() == InventoryStatus.RELEASED){
                continue;
            }

            // Step 5: Illegal: purchased cannot be released
            if(reservation.getStatus() == InventoryStatus.PURCHASED){
                throw new InvalidReservationStateException(
                        "Cannot release PURCHASED reservation for order " + orderId
                );
            }

            // Step 6: Restore stock safely
            Product product = productRepository.findById(reservation.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(reservation.getProductId()));

            product.increaseStock(reservation.getQuantity());

            // Step 7: Mark reservation released
            reservation.release();
        }
        // Step 8: Flush - stock & reservation update happens, detect conflicts now
        productRepository.flush();
        reservationRepository.flush();
    }
}
