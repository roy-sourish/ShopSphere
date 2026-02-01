package com.shopsphere.cart.controller;

import com.shopsphere.cart.domain.Cart;
import com.shopsphere.cart.dto.AddCartItemRequest;
import com.shopsphere.cart.dto.CartResponse;
import com.shopsphere.cart.dto.UpdateCartItemQuantityRequest;
import com.shopsphere.cart.service.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * View Cart -
     * GET /api/v1/cart?userId=1
     */
    @GetMapping
    public CartResponse viewCart(@RequestParam @Min(1) Long userId){
        Cart cart = cartService.getActiveCart(userId);

        if(cart == null){
            return CartResponse.empty();
        }

        return CartResponse.from(cart);
    }

    /**
     * Add item -
     * POST /api/v1/cart/items
     */
    @PostMapping("/items")
    public CartResponse addItem(
            @RequestParam @Min(1) Long userId,
            @Valid @RequestBody AddCartItemRequest request
    ){
        Cart cart = cartService.addItem(
                userId,
                request.getProductId(),
                request.getQuantity()
        );

        return CartResponse.from(cart);
    }

    /**
     * Update quantity -
     * PATCH /api/v1/cart/items/{itemId}
     */
    @PatchMapping("/items/{itemId}")
    public CartResponse updateQuantity(
            @RequestParam @Min(1) Long userId,
            @PathVariable @Min(1) Long itemId,
            @Valid @RequestBody UpdateCartItemQuantityRequest request
    ){
        Cart cart = cartService.updateItemQuantity(userId, itemId, request.getQuantity());

        return CartResponse.from(cart);
    }

    /**
     * Remove item -
     * DELETE /api/v1/cart/items/{itemId}
     */
    @DeleteMapping("/items/{itemId}")
    public CartResponse removeItem(
            @RequestParam @Min(1) Long userId,
            @PathVariable @Min(1) Long itemId
    ){
        Cart cart = cartService.removeItem(userId, itemId);
        return CartResponse.from(cart);
    }
}
