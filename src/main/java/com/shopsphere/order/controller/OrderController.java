package com.shopsphere.order.controller;

import com.shopsphere.order.domain.Order;
import com.shopsphere.order.dto.CreateOrderRequest;
import com.shopsphere.order.dto.OrderResponse;
import com.shopsphere.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // View Pending order
    @GetMapping("/pending")
    public ResponseEntity<OrderResponse> getPendingOrder(@RequestParam Long userId) {
        return orderService.getPendingOrder(userId)
                .map(order -> ResponseEntity.ok(OrderResponse.from(order)))
                .orElse(ResponseEntity.ok(OrderResponse.empty()));
    }

    // View Confirmed Order
    @GetMapping("/confirmed")
    public ResponseEntity<List<OrderResponse>> getConfirmedOrder(@RequestParam Long userId) {
        return ResponseEntity.ok(
                orderService.getConfirmedOrders(userId)
                        .stream()
                        .map(OrderResponse::from)
                        .toList()
        );
    }

    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<OrderResponse> confirmOrder(
            @PathVariable Long orderId,
            @RequestParam Long userId
    ) {
        Order confirmedOrder = orderService.confirmOrder(orderId, userId);
        return ResponseEntity.ok(OrderResponse.from(confirmedOrder));
    }
}
