package com.shopsphere.cart.domain;

import com.shopsphere.product.domain.Product;
import com.shopsphere.user.domain.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Cart -> User (Unidirectional) <br>
     * Cart belongs to one user.
     *
     */
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * Cart Lifecycle status
     *
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CartStatus status;

    /**
     * Cart -> CartItems (Bidirectional Parent -> Children) <br>
     * This tells Hibernate not to create another foreign key.
     * The relationship is already managed by CartItem.cart
     *
     */
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    protected Cart() {

    }

    public Cart(User user) {
        this.user = user;
        this.status = CartStatus.ACTIVE;
    }

    public Long getId() {
        return id;
    }

    public CartStatus getStatus() {
        return status;
    }

    public List<CartItem> getItems() {
        return items;
    }

    private void ensureActive() {
        if (this.status != CartStatus.ACTIVE) {
            throw new IllegalStateException("Cart is not active");
        }
    }

    public void addItem(Product product, int quantity) {
        if(quantity <= 0){
            throw new IllegalArgumentException("Quantity must be positive");
        }

        ensureActive();

        // Check if item already exists
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(product.getId())) {
                item.increaseQuantity(quantity);
                return;
            }
        }

        // Otherwise create new cart item
        CartItem newItem = new CartItem(this, product, quantity);
        this.items.add(newItem);
    }

    public void removeItem(CartItem item) {
        ensureActive();
        this.items.remove(item);
    }

    public void checkout(){
        ensureActive();
        this.status = CartStatus.CHECKED_OUT;
    }

    public boolean isEmpty(){
        return this.items.isEmpty();
    }
}
