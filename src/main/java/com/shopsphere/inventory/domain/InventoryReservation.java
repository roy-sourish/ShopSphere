package com.shopsphere.inventory.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "inventory_reservations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_inventory_idempotency", columnNames = "idempotency_key"),
                @UniqueConstraint(name = "uq_inventory_order_product", columnNames = {"order_id", "product_id"})
        }
)
public class InventoryReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryStatus status;

    @Column(name = "idempotency_key", nullable = false, updatable = false)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected InventoryReservation() {

    }

    public InventoryReservation(Long productId, Long orderId, Integer quantity, String idempotencyKey) {

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Reservation quantity must be positive");
        }

        this.productId = productId;
        this.orderId = orderId;
        this.quantity = quantity;
        this.idempotencyKey = idempotencyKey;

        this.status = InventoryStatus.RESERVED;
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

    // ----------------------------
    // Domain Lifecycle Transitions
    // ----------------------------

    public void markPurchased() {
        if (this.status != InventoryStatus.RESERVED) {
            throw new IllegalStateException("Only RESERVED reservations can be purchased");
        }
        this.status = InventoryStatus.PURCHASED;
    }

    public void release() {
        if (this.status != InventoryStatus.RESERVED) {
            throw new IllegalStateException("Only RESERVED reservations can be released");
        }
        this.status = InventoryStatus.RELEASED;
    }

    // ----------------------------
    // Getters (NO Setters)
    // ----------------------------

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public InventoryStatus getStatus() {
        return status;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

}
