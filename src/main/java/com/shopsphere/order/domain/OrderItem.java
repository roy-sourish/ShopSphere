package com.shopsphere.order.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "order_items",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"order_id", "productId"})
        }
)
public class OrderItem {
    private static final int MONEY_SCALE = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name_snapshot", nullable = false)
    private String productNameSnapshot;

    @Column(name = "unit_price_snapshot", nullable = false)
    private BigDecimal unitPriceSnapshot;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal lineTotal;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected OrderItem(){

    }

    public OrderItem(Order order, Long productId, String productNameSnapshot,
                     BigDecimal unitPriceSnapshot, Integer quantity) {

        Objects.requireNonNull(order, "Order cannot be null");
        Objects.requireNonNull(productId, "ProductId cannot be null");
        Objects.requireNonNull(productNameSnapshot, "Product name cannot be null");
        Objects.requireNonNull(unitPriceSnapshot, "Unit price cannot be null");
        Objects.requireNonNull(quantity, "Quantity price cannot be null");

        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be positive");

        if (unitPriceSnapshot.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Unit price cannot be negative");

        this.order = order;
        this.productId = productId;
        this.productNameSnapshot = productNameSnapshot;

        this.unitPriceSnapshot = unitPriceSnapshot;
        this.quantity = quantity;

        this.lineTotal = this.unitPriceSnapshot
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    @PrePersist
    void onCreate(){
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate(){
        this.updatedAt = Instant.now();
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductNameSnapshot() {
        return productNameSnapshot;
    }

    public BigDecimal getUnitPriceSnapshot() {
        return unitPriceSnapshot;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }
}
