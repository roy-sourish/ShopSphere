package com.shopsphere.product.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Version
    private Long version;

    protected Product() {

    }

    public Product(String sku, String name, BigDecimal price, Integer stockQuantity) {
        this.sku = sku;
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Long getVersion() {
        return version;
    }

    public void changeName(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be blank");
        }

        this.name = newName;
    }

    public void changeStock(Integer newStock) {
        if (newStock == null) {
            throw new IllegalArgumentException("Stock cannot be null");
        }

        if (newStock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }

        this.stockQuantity = newStock;
    }
}
