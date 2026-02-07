package com.shopsphere.inventory.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "inventory_reservations")
public class InventoryReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "cart_id", nullable = false)
    private Long cartId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected InventoryReservation() {
    }

    public InventoryReservation(Long userId,
                                Long cartId,
                                Long productId,
                                Integer quantity,
                                Instant expiresAt) {
        this.userId = userId;
        this.cartId = cartId;
        this.productId = productId;
        this.quantity = quantity;
        this.expiresAt = expiresAt;
        this.status = ReservationStatus.ACTIVE;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getCartId() {
        return cartId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void attachOrder(Long orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order id cannot be null");
        }
        this.orderId = orderId;
    }

    public void markConsumed() {
        ensureActive();
        this.status = ReservationStatus.CONSUMED;
    }

    public void markExpired() {
        ensureActive();
        this.status = ReservationStatus.EXPIRED;
    }

    public void markCancelled() {
        ensureActive();
        this.status = ReservationStatus.CANCELLED;
    }

    public boolean isExpired(Instant now) {
        return this.expiresAt.isBefore(now) || this.expiresAt.equals(now);
    }

    private void ensureActive() {
        if (this.status != ReservationStatus.ACTIVE) {
            throw new IllegalStateException("Reservation is not active");
        }
    }
}
