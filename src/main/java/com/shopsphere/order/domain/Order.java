package com.shopsphere.order.domain;

import com.shopsphere.cart.domain.Cart;
import com.shopsphere.cart.domain.CartItem;
import com.shopsphere.user.domain.User;
import jakarta.persistence.*;
import org.springframework.security.core.parameters.P;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Entity
@Table(name = "orders")
public class Order {
    private static final int MONEY_SCALE = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "currency_code", nullable = false)
    private String currencyCode;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<OrderItem> items = new ArrayList<>();

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Order(){

    }

    public Order(User user, String currencyCode) {
        this.user = user;
        this.currencyCode = currencyCode;
        this.totalAmount = BigDecimal.ZERO;
        this.status = OrderStatus.PENDING;
    }

    // Builds up order from cart.
    public static Order fromCart(Cart cart, String currencyCode){

        Objects.requireNonNull(cart, "Cart cannot be null");

        if(cart.isEmpty()){
            throw new IllegalStateException("Cannot create order from empty cart");
        }

        Order order = new Order(cart.getUser(), currencyCode);

        for (CartItem cartItem : cart.getItems()){
            OrderItem item = new OrderItem(
                    order,
                    cartItem.getProduct().getId(),
                    cartItem.getProduct().getName(),
                    cartItem.getProduct().getPrice(),
                    cartItem.getQuantity()
            );
            order.items.add(item);
            order.totalAmount = order.totalAmount.add(item.getLineTotal());
        }
        order.totalAmount = order.totalAmount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        return order;
    }

    public void confirm(){
        ensurePending();
        this.status = OrderStatus.CONFIRMED;
    }

    private void ensurePending(){
        if(this.status != OrderStatus.PENDING)
            throw new IllegalStateException("Order is not in PENDING state");
    }

    private static String validateCurrency(String currencyCode) {
        Objects.requireNonNull(currencyCode, "Currency cannot be null");

        if (currencyCode.length() != 3)
            throw new IllegalArgumentException("Currency must follow ISO-4217 format");

        return currencyCode.toUpperCase(Locale.ROOT);
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

    public List<OrderItem> getItems() {
        return items;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public User getUser() {
        return user;
    }

    public Long getId() {
        return id;
    }
}
