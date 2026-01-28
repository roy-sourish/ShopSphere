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

    @PrePersist
    void onCreate(){
        this.createdAt = Instant.now();
    }

    @Version
    private Long version;

    protected Product() {

    }

    public Product(String sku, String name, BigDecimal price, Integer stockQuantity) {

        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU cannot be blank");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be blank");
        }

        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }

        if (stockQuantity == null || stockQuantity < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }

        this.sku = sku.trim().toUpperCase();
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
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

    public void increaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        this.stockQuantity += quantity;
    }


    public void decreaseStock(int quantity){
        if(quantity <= 0){
            throw new IllegalArgumentException("Quantity must be positive");
        }

        if(this.stockQuantity < quantity){
            throw new IllegalStateException("Insufficient stock");
        }

        this.stockQuantity -= quantity;
    }
}
