package com.shopsphere.cart;

import com.shopsphere.cart.domain.Cart;
import com.shopsphere.cart.domain.CartStatus;
import com.shopsphere.cart.exception.CartItemNotFoundException;
import com.shopsphere.cart.repository.CartRepository;
import com.shopsphere.cart.service.CartService;
import com.shopsphere.product.domain.Product;
import com.shopsphere.product.repository.ProductRepository;
import com.shopsphere.user.domain.User;
import com.shopsphere.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

//import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
//import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
@Transactional
public class CartServiceTest {
    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    private User user;
    private Product product;

    @BeforeEach
    void setup() {
        // Create a test user
        user = new User("cart@test.com", "password123");
        userRepository.saveAndFlush(user);

        // Create a test product
        product = new Product(
                "SKU-CART-1",
                "Test Product",
                new BigDecimal("100.00"),
                10
        );
        productRepository.saveAndFlush(product);
    }

    /**
     * Test 1 — Lazy Cart Creation Works <br>
     * User adds item → cart is created automatically.
     */
    @Test
    void addItem_shouldCreateCartIfNotExists() {

        // Act
        Cart cart = cartService.addItem(user.getId(), product.getId(), 2);

        // Assert
        assertThat(cart.getId()).isNotNull();
        assertThat(cart.getStatus()).isEqualTo(CartStatus.ACTIVE);
        assertThat(cart.getItems()).hasSize(1);
    }

    /**
     * Test 2 — Adding Same Product Increases Quantity
     */
    @Test
    void addItem_shouldIncreaseQuantityIfProductAlreadyExists() {

        // Add once
        cartService.addItem(user.getId(), product.getId(), 2);

        // Add again
        Cart cart = cartService.addItem(user.getId(), product.getId(), 3);

        // Assert
        assertThat(cart.getItems()).hasSize(1);

        assertThat(cart.getItems().get(0).getQuantity())
                .isEqualTo(5);
    }

    /**
     * Test 3 — Update Quantity Works
     */
    @Test
    void updateItemQuantity_shouldChangeQuantity() {

        cartService.addItem(user.getId(), product.getId(), 2);

        Cart persisted = cartService.getExistingActiveCart(user.getId());
        Long itemId = persisted.getItems().get(0).getId();

        Cart updated =
                cartService.updateItemQuantity(user.getId(), itemId, 7);

        assertThat(updated.getItems().get(0).getQuantity())
                .isEqualTo(7);
    }

    /**
     * Test 4 — Quantity 0 Removes Item
     */
    @Test
    void updateItemQuantity_zeroShouldRemoveItem() {

        cartService.addItem(user.getId(), product.getId(), 2);

        Cart persisted = cartService.getExistingActiveCart(user.getId());
        Long itemId = persisted.getItems().get(0).getId();

        Cart updated =
                cartService.updateItemQuantity(user.getId(), itemId, 0);

        assertThat(updated.getItems()).isEmpty();
    }

    /**
     * Test 5 — Remove Item Explicitly Works
     */
    @Test
    void removeItem_shouldDeleteCartItem() {

        cartService.addItem(user.getId(), product.getId(), 2);

        Cart persisted = cartService.getExistingActiveCart(user.getId());
        Long itemId = persisted.getItems().get(0).getId();

        Cart updated =
                cartService.removeItem(user.getId(), itemId);

        assertThat(updated.getItems()).isEmpty();
    }

    /**
     * Test 6 — Viewing Cart When Empty Returns Null
     */
    @Test
    void getActiveCart_shouldReturnNullIfNoCartExists() {

        Cart cart = cartService.getActiveCart(user.getId());

        assertThat(cart).isNull();
    }

    /**
     * Test 7 — Updating Nonexistent Item Throws Exception
     */
    @Test
    void updateItemQuantity_shouldThrowIfItemNotFound() {

        cartService.addItem(user.getId(), product.getId(), 2);

        assertThatThrownBy(() ->
                cartService.updateItemQuantity(user.getId(), 999L, 5)
        ).isInstanceOf(CartItemNotFoundException.class);
    }

    /**
     * Removing last item does NOT delete cart
     */
    @Test
    void removingLastItem_shouldKeepCartButEmptyItems() {

        cartService.addItem(user.getId(), product.getId(), 1);

        Cart persisted = cartService.getExistingActiveCart(user.getId());
        Long cartId = persisted.getId();
        Long itemId = persisted.getItems().get(0).getId();

        cartService.removeItem(user.getId(), itemId);

        Cart stillExists = cartRepository.findById(cartId).orElseThrow();

        assertThat(stillExists.getItems()).isEmpty();
        assertThat(stillExists.getStatus()).isEqualTo(CartStatus.ACTIVE);
    }

}
