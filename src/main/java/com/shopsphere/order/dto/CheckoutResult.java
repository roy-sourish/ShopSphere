package com.shopsphere.order.dto;

import com.shopsphere.order.domain.Order;

public class CheckoutResult {
    private final Order order;
    private final boolean replayed;

    public CheckoutResult(Order order, boolean replayed) {
        this.order = order;
        this.replayed = replayed;
    }

    public Order getOrder() {
        return order;
    }

    public boolean isReplayed() {
        return replayed;
    }
}
