package com.shopsphere.inventory.service;

import com.shopsphere.cart.domain.Cart;
import com.shopsphere.cart.domain.CartItem;
import com.shopsphere.inventory.domain.InventoryReservation;
import com.shopsphere.inventory.domain.ReservationStatus;
import com.shopsphere.inventory.exception.ReservationExpiredException;
import com.shopsphere.inventory.exception.ReservationNotFoundException;
import com.shopsphere.inventory.repository.InventoryReservationRepository;
import com.shopsphere.product.domain.Product;
import com.shopsphere.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class InventoryReservationService {
    private static final Duration DEFAULT_RESERVATION_TTL = Duration.ofMinutes(15);

    private final InventoryReservationRepository reservationRepository;
    private final ProductRepository productRepository;

    public InventoryReservationService(InventoryReservationRepository reservationRepository,
                                       ProductRepository productRepository) {
        this.reservationRepository = reservationRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public List<InventoryReservation> reserveCartItems(Cart cart) {
        Instant expiresAt = Instant.now().plus(DEFAULT_RESERVATION_TTL);
        List<InventoryReservation> reservations = new ArrayList<>();

        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            product.decreaseStock(item.getQuantity());
            InventoryReservation reservation = new InventoryReservation(
                    cart.getUser().getId(),
                    cart.getId(),
                    product.getId(),
                    item.getQuantity(),
                    expiresAt
            );
            reservations.add(reservation);
        }

        return reservationRepository.saveAll(reservations);
    }

    @Transactional(readOnly = true)
    public void assertActiveReservationsForOrder(Long orderId) {
        List<InventoryReservation> reservations = reservationRepository.findByOrderId(orderId);
        if (reservations.isEmpty()) {
            throw new ReservationNotFoundException("order", orderId);
        }

        Instant now = Instant.now();
        for (InventoryReservation reservation : reservations) {
            if (reservation.getStatus() != ReservationStatus.ACTIVE || reservation.isExpired(now)) {
                throw new ReservationExpiredException(reservation.getId());
            }
        }
    }

    @Transactional(noRollbackFor = ReservationExpiredException.class)
    public void attachOrderToReservations(Long cartId, Long orderId) {
        List<InventoryReservation> reservations = reservationRepository.findByCartId(cartId);
        if (reservations.isEmpty()) {
            throw new ReservationNotFoundException("cart", cartId);
        }

        Instant now = Instant.now();
        for (InventoryReservation reservation : reservations) {
            if (reservation.getStatus() != ReservationStatus.ACTIVE) {
                throw new ReservationExpiredException(reservation.getId());
            }

            if (reservation.isExpired(now)) {
                releaseReservationStock(reservation);
                reservation.markExpired();
                throw new ReservationExpiredException(reservation.getId());
            }

            reservation.attachOrder(orderId);
        }
    }

    @Transactional(noRollbackFor = ReservationExpiredException.class)
    public void consumeReservations(Long orderId) {
        List<InventoryReservation> reservations = reservationRepository
                .findByOrderIdAndStatus(orderId, ReservationStatus.ACTIVE);

        if (reservations.isEmpty()) {
            throw new ReservationNotFoundException("order", orderId);
        }

        Instant now = Instant.now();
        for (InventoryReservation reservation : reservations) {
            if (reservation.isExpired(now)) {
                releaseReservationStock(reservation);
                reservation.markExpired();
                throw new ReservationExpiredException(reservation.getId());
            }
            reservation.markConsumed();
        }
    }

    @Transactional
    public void releaseExpiredReservations() {
        Instant now = Instant.now();
        List<InventoryReservation> expiredReservations = reservationRepository
                .findByStatusAndExpiresAtBefore(ReservationStatus.ACTIVE, now);

        for (InventoryReservation reservation : expiredReservations) {
            releaseReservationStock(reservation);
            reservation.markExpired();
        }
    }

    @Transactional
    public void releaseReservationsForOrder(Long orderId) {
        List<InventoryReservation> reservations = reservationRepository.findByOrderId(orderId);
        if (reservations.isEmpty()) {
            throw new ReservationNotFoundException("order", orderId);
        }

        for (InventoryReservation reservation : reservations) {
            if (reservation.getStatus() == ReservationStatus.ACTIVE) {
                releaseReservationStock(reservation);
                reservation.markCancelled();
            }
        }
    }

    @Transactional
    public void releaseReservationsForCart(Long cartId) {
        List<InventoryReservation> reservations = reservationRepository.findByCartId(cartId);
        if (reservations.isEmpty()) {
            return;
        }

        for (InventoryReservation reservation : reservations) {
            if (reservation.getStatus() == ReservationStatus.ACTIVE) {
                releaseReservationStock(reservation);
                reservation.markCancelled();
            }
        }
    }

    private void releaseReservationStock(InventoryReservation reservation) {
        Product product = productRepository.findById(reservation.getProductId())
                .orElseThrow(() -> new IllegalStateException(
                        "Product not found for reservation (id=" + reservation.getId() + ")"
                ));
        product.increaseStock(reservation.getQuantity());
    }
}
