package com.shopsphere.cart.domain;

import com.shopsphere.product.domain.Product;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "cart_items",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"cart_id", "product_id"})
        }
)
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Many items belong to one cart
     * */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    /**
     * Each cart item refers to one product
     * */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    protected CartItem(){

    }

    public CartItem(Cart cart, Product product, Integer quantity){
        if(quantity == null || quantity <= 0){
            throw new IllegalArgumentException("Quantity must be positive");
        }

        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void increaseQuantity(int amount){
        if(amount <= 0){
            throw new IllegalArgumentException("Increase amount must be positive");
        }
        this.quantity += amount;
        this.updatedAt = Instant.now();
    }

    public void changeQuantity(int newQuantity){
        if(newQuantity <= 0){
            throw new IllegalArgumentException("Quantity must be positive");
        }

        this.quantity = newQuantity;
        this.updatedAt = Instant.now();
    }
}
