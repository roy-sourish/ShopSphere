package com.shopsphere.order.dto;

import com.shopsphere.order.domain.Order;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class OrderResponse {
    private Long orderId;
    private String status;
    private String currencyCode;
    private BigDecimal totalAmount;
    private List<OrderItemResponse> items;

    public OrderResponse(Long orderId,
                         String status,
                         String currencyCode,
                         BigDecimal totalAmount,
                         List<OrderItemResponse> items
    ) {
        this.orderId = orderId;
        this.status = status;
        this.currencyCode = currencyCode;
        this.totalAmount = totalAmount;
        this.items = items;
    }

    public static OrderResponse empty(){
        return new OrderResponse(
                null,
                null,
                null,
                null,
                Collections.emptyList()
        );
    }

    public static OrderResponse from(Order order){
        return new OrderResponse(
                order.getId(),
                order.getStatus().name(),
                order.getCurrencyCode().toUpperCase(),
                order.getTotalAmount(),
                order.getItems().stream()
                        .map(o -> OrderItemResponse.from(o))
                        .toList()
        );
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getStatus() {
        return status;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }
}
