package com.shopsphere.order.domain;

import com.shopsphere.user.domain.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_orders_idempotency",
                        columnNames = "idempotency_key"
                )
        }
)
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Order belongs to one user.
     * Orders are permanent history → user deletion restricted at DB level.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    /**
     * Total snapshot amount at checkout time.
     */
    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    /**
     * Checkout idempotency key (prevents duplicate orders).
     */
    @Column(name = "idempotency_key", nullable = false, updatable = false)
    private String idempotencyKey;

    /**
     * Order → OrderItems (aggregate child)
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected Order() {
        // JPA only
    }

    public Order(User user, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency key must be provided");
        }

        this.user = user;
        this.idempotencyKey = idempotencyKey;

        this.status = OrderStatus.PENDING;
        this.totalAmount = BigDecimal.ZERO;
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

    public void addItemSnapshot(Long productId, String productName, BigDecimal unitPrice, int quantity){
        ensurePending();

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price must be positive");
        }

        OrderItem item = new OrderItem(
                this,
                productId,
                productName,
                unitPrice,
                quantity
        );

        this.items.add(item);

        // Update total immediately (snapshot correctness)
        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        this.totalAmount = this.totalAmount.add(lineTotal);
    }

    public void confirm() {
        ensurePending();
        this.status = OrderStatus.CONFIRMED;
    }

    public void cancel() {
        ensurePending();
        this.status = OrderStatus.CANCELLED;
    }

    private void ensurePending() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Only PENDING orders can be modified. Current status = " + status
            );
        }
    }

    public Long getId() {
        return id;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}