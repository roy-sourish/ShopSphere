package com.shopsphere.order;

import com.shopsphere.cart.domain.Cart;
import com.shopsphere.cart.domain.CartStatus;
import com.shopsphere.cart.repository.CartRepository;
import com.shopsphere.cart.service.CartService;
import com.shopsphere.order.domain.Order;
import com.shopsphere.order.domain.OrderItem;
import com.shopsphere.order.domain.OrderStatus;
import com.shopsphere.order.exception.EmptyCartException;
import com.shopsphere.order.exception.NoCheckedOutCartException;
import com.shopsphere.order.repository.OrderRepository;
import com.shopsphere.order.service.OrderService;
import com.shopsphere.product.domain.Product;
import com.shopsphere.product.repository.ProductRepository;
import com.shopsphere.user.domain.User;
import com.shopsphere.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
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

    @Test
    void createOrder_fromCheckedOutCart_createsPendingOrder() {
        User user = createUser();
        Product product = createProduct("SKU-ORDER-SERVICE-1", new BigDecimal("100.00"));
        cartService.addItem(user.getId(), product.getId(), 2);
        cartService.checkout(user.getId());

        Order order = orderService.createPendingOrder(user.getId(), "USD");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getItems()).hasSize(1);

        OrderItem item = order.getItems().get(0);
        assertThat(item.getProductId()).isEqualTo(product.getId());
        assertThat(item.getQuantity()).isEqualTo(2);
        assertThat(item.getLineTotal()).isEqualByComparingTo("200.00");

        // cart unchanged
        Cart reloadedCart = cartRepository
                .findCartWithItems(user.getId(), CartStatus.CHECKED_OUT)
                .orElseThrow();

        assertThat(reloadedCart.getItems()).hasSize(1);
    }

    @Test
    void createOrder_whenPendingExists_returnsSameOrder() {
        User user = createUser();
        Product product = createProduct("SKU-ORDER-SERVICE-2", new BigDecimal("150.00"));
        cartService.addItem(user.getId(), product.getId(), 2);
        cartService.checkout(user.getId());

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
    void createOrder_withoutCheckedOutCart_throwsNoCheckedOutCartException() {
        User user = createUser();

        assertThatThrownBy(() ->
                orderService.createPendingOrder(user.getId(), "USD")
        )
                .isInstanceOf(NoCheckedOutCartException.class)
                .hasMessageContaining("No checked out cart found");
    }

    @Test
    void createOrder_withEmptyCart_throwsEmptyCartException() {
        User user = createUser();

        // Create CHECKED_OUT cart but do not add any items
        Cart cart = cartService.getOrCreateActiveCart(user.getId());
        ReflectionTestUtils.setField(cart, "status", CartStatus.CHECKED_OUT);
        cartRepository.save(cart);

        assertThatThrownBy(() ->
                orderService.createPendingOrder(user.getId(), "USD")
        )
                .isInstanceOf(EmptyCartException.class)
                .hasMessageContaining("Cannot create order from empty cart");
    }
}
