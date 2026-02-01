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

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, UserRepository userRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Cart getOrCreateActiveCart(Long userId) {
        // Step 1: Ensure user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Step 2: Try to find ACTIVE cart
        return cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseGet(() -> {
                    // Step 3: Lazy create new cart
                    try {
                        Cart newCart = new Cart(user);
                        return cartRepository.save(newCart);
                    } catch (DataIntegrityViolationException ex){
                        // Another request created cart first -> retry fetch
                        return cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                                .orElseThrow(() -> ex);
                    }
                });
    }

    @Transactional(readOnly = true)
    public Cart getExistingActiveCart(Long userId){
        if(!userRepository.existsById(userId)){
            throw new UserNotFoundException(userId);
        }

        return cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseThrow(() -> new CartNotFoundException(userId));
    }

    @Transactional
    public Cart addItem(Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        // Step 1: Load product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // Step 2: Get or create cart
        Cart cart = getOrCreateActiveCart(userId);

        // Step 3: Delegate to domain logic
        cart.addItem(product, quantity);

        // Step 4: Return cart(dirty checking persists changes)
        return cart;
    }

    @Transactional
    public Cart updateItemQuantity(Long userId, Long cartItemId, int newQuantity) {
        Cart cart = getExistingActiveCart(userId);

        // Find item inside cart
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException(cartItemId));

        // quantity == 0 means remove item
        if (newQuantity == 0) {
            cart.removeItem(item);
            return cart;
        } else if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        // Update item quantity
        item.changeQuantity(newQuantity);
        return cart;
    }

    @Transactional
    public Cart removeItem(Long userId, Long cartItemId) {
        Cart cart = getExistingActiveCart(userId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException(cartItemId));

        cart.removeItem(item);

        return cart;
    }

    @Transactional(readOnly = true)
    public Cart getActiveCart(Long userId) {
        return cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElse(null);
    }
}
