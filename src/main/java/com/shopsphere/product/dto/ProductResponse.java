package com.shopsphere.product.dto;

import com.shopsphere.product.domain.Product;

import java.math.BigDecimal;
import java.time.Instant;

public class ProductResponse {
    private Long id;
    private String sku;
    private String name;
    private BigDecimal price;
    private Integer stockQuantity;
    private Instant createdAt;

    public ProductResponse(Long id, String sku, String name, BigDecimal price,
                           Integer stockQuantity, Instant createdAt) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.createdAt = createdAt;
    }

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getCreatedAt()
        );
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
}
