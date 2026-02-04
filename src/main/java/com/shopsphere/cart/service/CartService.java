package com.shopsphere.cart.service;

import com.shopsphere.cart.domain.Cart;
import com.shopsphere.cart.domain.CartItem;
import com.shopsphere.cart.domain.CartStatus;
import com.shopsphere.cart.exception.CartItemNotFoundException;
import com.shopsphere.cart.exception.CartNotFoundException;
import com.shopsphere.cart.repository.CartRepository;
import com.shopsphere.product.domain.Product;
import com.shopsphere.product.exception.ProductNotFoundException;
import com.shopsphere.product.repository.ProductRepository;
import com.shopsphere.user.domain.User;
import com.shopsphere.user.exception.UserNotFoundException;
import com.shopsphere.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class CartService {
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, UserRepository userRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    /**
     * View active cart (returns empty if none exists).
     */
    @Transactional(readOnly = true)
    public Optional<Cart> getActiveCart(Long userId) {
        return cartRepository.findCartWithItems(userId, CartStatus.ACTIVE);
    }

    /**
     * Add item to cart (merge quantity if exists).
     */
    public Cart addItem(Long userId, Long productId, int quantity) {
        Cart cart = getOrCreateActiveCart(userId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        cart.addItem(product, quantity);

        return cart;
    }

    /**
     * Update quantity.
     * quantity == 0 means remove.
     */
    public void updateQuantity(Long userId, Long productId, int quantity) {
        Cart cart = getActiveCartOrThrow(userId);

        cart.updateItemQuantity(productId, quantity);
    }

    /**
     * Remove item fully.
     */
    public void removeItem(Long userId, Long productId) {
        Cart cart = getActiveCartOrThrow(userId);
        cart.removeItem(productId);
    }

    /**
     * Checkout closes cart.
     */
    public void checkout(Long userId) {
        Cart cart = getActiveCartOrThrow(userId);
        cart.checkout();
    }

    private Cart getActiveCartOrThrow(Long userId) {
        return cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseThrow(() -> new CartNotFoundException(userId));
    }

    /**
     * Lazy create ACTIVE cart if missing.
     * Handles race conditions using UNIQUE(user_id).
     */
    public Cart getOrCreateActiveCart(Long userId) {

        return cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseGet(() -> createCartSafely(userId));
    }

    private Cart createCartSafely(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        try {
            Cart cart = new Cart(user);
            return cartRepository.save(cart);

        } catch (DataIntegrityViolationException ex) {
            // Another request created the cart concurrently.
            return cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                    .orElseThrow(() -> new IllegalStateException(
                            "Cart creation race condition failed unexpectedly"
                    ));
        }
    }
}
