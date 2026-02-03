package com.shopsphere.order.controller;

import com.shopsphere.order.domain.Order;
import com.shopsphere.order.dto.CheckoutResult;
import com.shopsphere.order.dto.OrderResponse;
import com.shopsphere.order.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * POST /orders/checkout
     * Creates a new Order from the user's ACTIVE cart.
     * Idempotency-Key is REQUIRED.
     */
    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestParam Long userId
    ){
        CheckoutResult result = orderService.checkout(userId, idempotencyKey);
        OrderResponse body = OrderResponse.from(result.getOrder());

        // Replay return 200 OK
        if(result.isReplayed()){
            return ResponseEntity.ok(body);
        }

        // New Order -> return 201 Created with Location Header
        URI location = URI.create("/orders/" + result.getOrder().getId());

        return ResponseEntity.created(location).body(body);
    }

    /**
     * POST /orders/{id}/confirm <br>
     * Simulates payment success: <br>
     * RESERVED → PURCHASED <br>
     * PENDING → CONFIRMED <br>
     */
    @PostMapping("/{orderId}/confirm")
    @ResponseStatus(HttpStatus.OK)
    public OrderResponse confirm(@PathVariable Long orderId){
        Order order = orderService.confirm(orderId);

        return OrderResponse.from(order);
    }

    /**
     * POST /orders/{id}/cancel <br>
     * Cancels pending order: <br>
     * RESERVED → RELEASED  <br>
     * PENDING → CANCELLED  <br>
     */
    @PostMapping("/{orderId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public OrderResponse cancel(@PathVariable Long orderId){
        Order order = orderService.cancel(orderId);
        return OrderResponse.from(order);
    }

    @GetMapping("/{orderId}")
    @ResponseStatus(HttpStatus.OK)
    public OrderResponse getOrder(@PathVariable Long orderId, @RequestParam Long userId){
        return OrderResponse.from(orderService.getOrder(orderId, userId));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<OrderResponse> getUserOrders(@RequestParam Long userId){
        return orderService.getUserOrders(userId)
                .stream().map( order -> OrderResponse.from(order))
                .toList();
    }
}
