package com.shopsphere.order.dto;

import com.shopsphere.order.domain.Order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OrderResponse {
    private Long orderId;
    private String status;
    private BigDecimal totalAmount;
    private Instant createdAt;
    private List<OrderItemResponse> items;

    public static OrderResponse from(Order order) {
        OrderResponse dto = new OrderResponse();
        dto.orderId = order.getId();
        dto.status = order.getStatus().name();
        dto.totalAmount = order.getTotalAmount();
        dto.createdAt = order.getCreatedAt();

        dto.items = order.getItems()
                .stream()
                .map(OrderItemResponse::from)
                .toList();

        return dto;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }
}
