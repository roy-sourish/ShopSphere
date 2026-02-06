package com.shopsphere.order;

import com.shopsphere.cart.domain.Cart;
import com.shopsphere.cart.repository.CartRepository;
import com.shopsphere.cart.service.CartService;
import com.shopsphere.order.domain.Order;
import com.shopsphere.order.domain.OrderStatus;
import com.shopsphere.order.repository.OrderRepository;
import com.shopsphere.order.service.OrderService;
import com.shopsphere.product.domain.Product;
import com.shopsphere.product.repository.ProductRepository;
import com.shopsphere.user.domain.User;
import com.shopsphere.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class OrderConcurrencyIntegrationTest {

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

    @BeforeEach
    void clean() {
        orderRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    private User createUser() {
        return userRepository.save(
                new User("test@example.com", "password")
        );
    }

    private Product createProduct(String sku, BigDecimal price) {
        return productRepository.save(
                new Product(sku, "Test Product", price, 100)
        );
    }

    private Cart createCartWithItem(User user, Product product, int qty) {
        Cart cart = new Cart(user);
        cart.addItem(product, qty);
        return cartRepository.save(cart);
    }

    @Test
    void createOrder_concurrentRequests_createsOnlyOnePendingOrder() throws Exception {
        // Arrange
        User user = createUser();
        Product product = createProduct("SKU-1", BigDecimal.valueOf(1000));

        Cart cart = cartService.addItem(user.getId(), product.getId(), 1);
        cartRepository.saveAndFlush(cart);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<Order> task = () ->
                orderService.createPendingOrder(user.getId(), "USD");

        // Act
        Future<Order> f1 = executor.submit(task);
        Future<Order> f2 = executor.submit(task);

        Order o1 = f1.get();
        Order o2 = f2.get();

        executor.shutdown();

        // Assert: both calls return the same order
        assertThat(o1.getId()).isEqualTo(o2.getId());

        // Assert: DB has only one PENDING order
        List<Order> orders =
                orderRepository.findAll().stream()
                        .filter(o -> o.getUser().getId().equals(user.getId()))
                        .filter(o -> o.getStatus() == OrderStatus.PENDING)
                        .toList();

        assertThat(orders).hasSize(1);
    }
}


