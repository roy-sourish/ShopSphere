package com.shopsphere.cart.controller;

import com.shopsphere.cart.domain.Cart;
import com.shopsphere.cart.dto.AddCartItemRequest;
import com.shopsphere.cart.dto.CartResponse;
import com.shopsphere.cart.dto.UpdateCartItemQuantityRequest;
import com.shopsphere.cart.service.CartService;
import com.shopsphere.order.dto.CreateOrderRequest;
import com.shopsphere.order.dto.OrderResponse;
import com.shopsphere.order.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {
    private final CartService cartService;
    private final OrderService orderService;

    public CartController(CartService cartService, OrderService orderService) {
        this.cartService = cartService;
        this.orderService = orderService;
    }

    /**
     * View active cart (returns empty if none exists).
     */
    @GetMapping
    public ResponseEntity<CartResponse> viewCart(@RequestParam Long userId) {
        return cartService.getActiveCart(userId)
                .map(cart -> ResponseEntity.ok(CartResponse.from(cart)))
                .orElse(ResponseEntity.noContent().build());
    }

    /**
     * Add item to cart.
     */
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @RequestParam Long userId,
            @Valid @RequestBody AddCartItemRequest request
    ) {

        var cart = cartService.addItem(
                userId,
                request.getProductId(),
                request.getQuantity()
        );

        return ResponseEntity.ok(CartResponse.from(cart));
    }

    /**
     * Update quantity (0 removes item).
     */
    @PatchMapping("/items/{productId}")
    public ResponseEntity<Void> updateQuantity(
            @RequestParam Long userId,
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemQuantityRequest request
    ) {

        cartService.updateQuantity(userId, productId, request.getQuantity());
        return ResponseEntity.noContent().build();
    }

    /**
     * Remove item completely.
     */
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeItem(
            @RequestParam Long userId,
            @PathVariable Long productId
    ) {

        cartService.removeItem(userId, productId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Checkout closes cart.
     */
    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(
            @RequestParam Long userId,
            @Valid @RequestBody CreateOrderRequest request
    ) {

        var order = orderService.createPendingOrder(userId, request.getCurrencyCode());
        return ResponseEntity.ok(OrderResponse.from(order));
    }
}
