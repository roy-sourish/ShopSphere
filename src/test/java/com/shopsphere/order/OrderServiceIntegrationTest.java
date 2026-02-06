package com.shopsphere.order;

import com.shopsphere.cart.domain.Cart;
import com.shopsphere.cart.domain.CartStatus;
import com.shopsphere.cart.repository.CartRepository;
import com.shopsphere.cart.service.CartService;
import com.shopsphere.order.domain.Order;
import com.shopsphere.order.domain.OrderItem;
import com.shopsphere.order.domain.OrderStatus;
import com.shopsphere.order.exception.EmptyCartException;
import com.shopsphere.order.exception.NoActiveCartException;
import com.shopsphere.order.repository.OrderRepository;
import com.shopsphere.order.service.OrderService;
import com.shopsphere.product.domain.Product;
import com.shopsphere.product.repository.ProductRepository;
import com.shopsphere.user.domain.User;
import com.shopsphere.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.antlr.v4.runtime.misc.LogManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SpringBootTest
@Transactional
public class OrderServiceIntegrationTest {
    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;


    private User createUser() {
        return userRepository.save(
                new User("orderService@example.com", "password")
        );
    }

    private Product createProduct(String sku, BigDecimal price) {
        return productRepository.save(
                new Product(sku, "Order Service Test Product", price, 100)
        );
    }

    private Cart createCartWithItem(User user, Product product, int qty) {
        Cart cart = new Cart(user);
        cart.addItem(product, qty);
        return cartRepository.save(cart);
    }

    @Test
    void createOrder_fromActiveCart_createsPendingOrder() {
        User user = createUser();
        Product product = createProduct("SKU-ORDER-SERVICE-1", new BigDecimal("100.00"));
        Cart cart = createCartWithItem(user, product, 2);

        Order order = orderService.createPendingOrder(user.getId(), "USD");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getItems()).hasSize(1);

        OrderItem item = order.getItems().get(0);
        assertThat(item.getProductId()).isEqualTo(product.getId());
        assertThat(item.getQuantity()).isEqualTo(2);
        assertThat(item.getLineTotal()).isEqualByComparingTo("200.00");

        // cart unchanged
        Cart reloadedCart = cartRepository
                .findCartWithItems(user.getId(), CartStatus.ACTIVE)
                .orElseThrow();

        assertThat(reloadedCart.getItems()).hasSize(1);
    }

    @Test
    void createOrder_whenPendingExists_returnsSameOrder() {
        User user = createUser();
        Product product = createProduct("SKU-ORDER-SERVICE-2", new BigDecimal("150.00"));
        createCartWithItem(user, product, 2);

        // First call creates the order
        Order firstOrder = orderService.createPendingOrder(user.getId(), "USD");

        // Second call must return the same order
        Order secondOrder = orderService.createPendingOrder(user.getId(), "USD");

        assertThat(secondOrder.getId()).isEqualTo(firstOrder.getId());
        assertThat(secondOrder.getStatus()).isEqualTo(OrderStatus.PENDING);

        // Ensure only one pending order exists in DB
        long pendingCount = orderRepository.findAll().stream()
                .filter(o -> o.getUser().getId().equals(user.getId()))
                .filter(o -> o.getStatus() == OrderStatus.PENDING)
                .count();

        assertThat(pendingCount).isEqualTo(1);
    }

    @Test
    void createOrder_withoutActiveCart_throwsNoActiveCartException() {
        User user = createUser();

        assertThatThrownBy(() ->
                orderService.createPendingOrder(user.getId(), "USD")
        )
                .isInstanceOf(NoActiveCartException.class)
                .hasMessageContaining("No active cart found");
    }

    @Test
    void createOrder_withEmptyCart_throwsEmptyCartException() {
        User user = createUser();

        // Create ACTIVE cart but do not add any items
        Cart cart = cartService.getOrCreateActiveCart(user.getId());
        cartRepository.save(cart);

        assertThatThrownBy(() ->
                orderService.createPendingOrder(user.getId(), "USD")
        )
                .isInstanceOf(EmptyCartException.class)
                .hasMessageContaining("Cannot create order from empty cart");
    }
}
