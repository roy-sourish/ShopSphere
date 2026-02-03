package com.shopsphere.order.exception;

public class OrderAccessDeniedException extends RuntimeException {
    public OrderAccessDeniedException(Long orderId) {

        super("You do not have access to order id = " + orderId);
    }
}
