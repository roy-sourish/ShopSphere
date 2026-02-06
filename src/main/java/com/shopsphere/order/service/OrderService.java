package com.shopsphere.order.service;

import com.shopsphere.cart.domain.Cart;
import com.shopsphere.cart.domain.CartStatus;
import com.shopsphere.cart.repository.CartRepository;
import com.shopsphere.common.exception.OptimisticConflictException;
import com.shopsphere.order.domain.Order;
import com.shopsphere.order.domain.OrderStatus;
import com.shopsphere.order.exception.EmptyCartException;
import com.shopsphere.order.exception.NoActiveCartException;
import com.shopsphere.order.exception.OrderNotFoundException;
import com.shopsphere.order.repository.OrderRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
    }

    /**
     * Get PENDING order
     */
    @Transactional(readOnly = true)
    public Optional<Order> getPendingOrder(Long userId) {
        return orderRepository.findOrderWithItems(userId, OrderStatus.PENDING);
    }

    /**
     * Get CONFIRMED order
     */
    @Transactional(readOnly = true)
    public Optional<Order> getConfirmedOrder(Long userId) {
        return orderRepository.findOrderWithItems(userId, OrderStatus.CONFIRMED);
    }


    public Order createPendingOrder(Long userId, String currencyCode){
        try {
            return createPendingOrderTx(userId, currencyCode);
        } catch (DataIntegrityViolationException ex) {
            return orderRepository.findByUserIdAndStatus(userId, OrderStatus.PENDING)
                    .orElseThrow(() -> new OptimisticConflictException("Order", userId));
        }
    }

    /**
     * Create Cart -> Order Snapshot
     */
    @Transactional
    protected Order createPendingOrderTx(Long userId, String currencyCode) {
        // Step 1: Enforce single pending order
        return orderRepository.findByUserIdAndStatus(userId, OrderStatus.PENDING)
                .orElseGet(() -> createFromCart(userId, currencyCode));
    }

    private Order createFromCart(Long userId, String currencyCode) {
        // Step 2: Load active cart with items
        Cart cart = cartRepository.findCartWithItems(userId, CartStatus.ACTIVE)
                .orElseThrow(() -> new NoActiveCartException(userId));

        if (cart.isEmpty()) {
            throw new EmptyCartException();
        }

        // Step 3: Snapshot cart
        Order order = Order.fromCart(cart, currencyCode);
        return orderRepository.saveAndFlush(order);

    }

    @Transactional
    public Order confirmOrder(Long orderId, Long userId) {
        Order order = orderRepository.findByIdAndUserIdWithItems(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        order.confirm();
        return order;
    }
}
