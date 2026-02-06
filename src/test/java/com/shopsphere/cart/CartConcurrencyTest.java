package com.shopsphere.cart;


import com.shopsphere.cart.domain.Cart;
import com.shopsphere.cart.domain.CartStatus;
import com.shopsphere.cart.repository.CartRepository;
import com.shopsphere.cart.service.CartService;
import com.shopsphere.order.repository.OrderRepository;
import com.shopsphere.product.domain.Product;
import com.shopsphere.product.repository.ProductRepository;
import com.shopsphere.user.domain.User;
import com.shopsphere.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class CartConcurrencyTest {
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
    void cleanDB(){
        orderRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void concurrentRequests_createOnlyOneActiveCart() throws Exception {
        // Arrange
        User user = userRepository.save(new User("cartSrvConcur@test.com", "testPassword"));
        Product product = productRepository.save(
                new Product("SKU-CONCR-CART-SRV", "Cart Service Product", BigDecimal.valueOf(100), 10)
        );

        int threads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        Runnable task = () -> {
            ready.countDown();
            try {
                start.await(); // all threads start together
                cartService.addItem(user.getId(), product.getId(), 1);
            } catch (Exception ignored) {
            } finally {
                done.countDown();
            }
        };

        for (int i = 0; i < threads; i++) {
            executor.submit(task);
        }

        ready.await();
        start.countDown(); // fire!
        done.await();

        // Assert
        List<Cart> carts = cartRepository.findAll();

        assertThat(carts).hasSize(1);

        Cart cart = cartRepository
                .findCartWithItems(user.getId(), CartStatus.ACTIVE)
                .orElseThrow();

        assertThat(cart.getStatus()).isEqualTo(CartStatus.ACTIVE);
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isGreaterThanOrEqualTo(1);
    }
}
