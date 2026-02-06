package com.shopsphere.cart;

import com.shopsphere.cart.domain.Cart;
import com.shopsphere.cart.domain.CartStatus;
import com.shopsphere.cart.exception.CartNotFoundException;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class CartServiceIntegrationTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    private User user;
    private Product product;

    @BeforeEach
    void setup() {
        user = userRepository.save(new User(
                "roy2@test.com",
                "password"
        ));

        product = productRepository.save(new Product(
                "iPhone 15",
                "SKU-IPHONE",
                BigDecimal.valueOf(79000),
                10
        ));
    }

    // ============================================
    // TEST 1: Lazy cart creation
    // ============================================

    @Test
    void addItem_createsCartIfMissing() {

        Cart cart = cartService.addItem(user.getId(), product.getId(), 2);

        assertThat(cart.getStatus()).isEqualTo(CartStatus.ACTIVE);
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(2);
    }

    // ============================================
    // TEST 2: Quantity merge
    // ============================================

    @Test
    void addItem_mergesQuantityIfProductAlreadyExists() {

        cartService.addItem(user.getId(), product.getId(), 2);
        Cart cart = cartService.addItem(user.getId(), product.getId(), 3);

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(5);
    }

    // ============================================
    // TEST 3: Update quantity works
    // ============================================

    @Test
    void updateQuantity_changesQuantityCorrectly() {

        cartService.addItem(user.getId(), product.getId(), 2);

        cartService.updateQuantity(user.getId(), product.getId(), 7);

        Cart cart = cartService.getActiveCart(user.getId()).orElseThrow();
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(7);
    }

    // ============================================
    // TEST 4: Quantity = 0 removes item
    // ============================================

    @Test
    void updateQuantity_zeroRemovesItem() {

        cartService.addItem(user.getId(), product.getId(), 2);

        cartService.updateQuantity(user.getId(), product.getId(), 0);

        Cart cart = cartService.getActiveCart(user.getId()).orElseThrow();
        assertThat(cart.getItems()).isEmpty();
    }

    // ============================================
    // TEST 5: Remove item explicitly
    // ============================================

    @Test
    void removeItem_deletesItemFromCart() {

        cartService.addItem(user.getId(), product.getId(), 2);

        cartService.removeItem(user.getId(), product.getId());

        Cart cart = cartService.getActiveCart(user.getId()).orElseThrow();
        assertThat(cart.getItems()).isEmpty();
    }

    // ============================================
    // TEST 6: Checkout locks cart
    // ============================================

    @Test
    void checkout_changesStatusToCheckedOut() {

        // Arrange: create cart with item
        Cart cart = cartService.addItem(user.getId(), product.getId(), 1);
        Long cartId = cart.getId();

        // Act: checkout
        cartService.checkout(user.getId());

        // Assert: reload cart by ID (not ACTIVE lookup)
        Cart updated = cartRepository.findById(cartId).orElseThrow();

        assertThat(updated.getStatus()).isEqualTo(CartStatus.CHECKED_OUT);
    }


//     ============================================
//     TEST 7: Cannot mutate after checkout
//     ============================================

    @Test
    void addItem_afterCheckout_createsNewActiveCart() {

        // Arrange: checkout first cart
        Cart firstCart = cartService.addItem(user.getId(), product.getId(), 1);
        cartService.checkout(user.getId());

        // Act: add item again
        Cart secondCart = cartService.addItem(user.getId(), product.getId(), 2);

        // Assert: new cart is ACTIVE
        assertThat(secondCart.getStatus()).isEqualTo(CartStatus.ACTIVE);

        // Assert: different cart instance
        assertThat(secondCart.getId()).isNotEqualTo(firstCart.getId());
    }

    // ============================================
    // TEST 8: CartNotFoundException if none exists
    // ============================================

    @Test
    void updateQuantity_throwsIfCartMissing() {

        assertThatThrownBy(() ->
                cartService.updateQuantity(user.getId(), product.getId(), 2)
        ).isInstanceOf(CartNotFoundException.class);
    }

}
