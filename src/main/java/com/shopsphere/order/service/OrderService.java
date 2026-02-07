package com.shopsphere.order.service;

import com.shopsphere.cart.domain.Cart;
import com.shopsphere.cart.domain.CartStatus;
import com.shopsphere.cart.repository.CartRepository;
import com.shopsphere.common.exception.OptimisticConflictException;
import com.shopsphere.inventory.service.InventoryReservationService;
import com.shopsphere.order.domain.Order;
import com.shopsphere.order.domain.OrderStatus;
import com.shopsphere.order.exception.EmptyCartException;
import com.shopsphere.order.exception.NoCheckedOutCartException;
import com.shopsphere.order.exception.OrderNotFoundException;
import com.shopsphere.order.repository.OrderRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final InventoryReservationService reservationService;

    public OrderService(OrderRepository orderRepository,
                        CartRepository cartRepository,
                        InventoryReservationService reservationService) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.reservationService = reservationService;
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
    public List<Order> getConfirmedOrders(Long userId) {
        return orderRepository.findConfirmedOrdersWithItems(userId, OrderStatus.CONFIRMED);
    }


    public Order createPendingOrder(Long userId, String currencyCode){
        try {
            Order order = createPendingOrderTx(userId, currencyCode);
            return loadPendingOrderWithItems(order.getId(), userId);
        } catch (DataIntegrityViolationException ex) {
            return orderRepository.findByUserIdAndStatus(userId, OrderStatus.PENDING)
                    .orElseThrow(() -> new OptimisticConflictException("Order", userId));
        }
    }

    @Transactional(readOnly = true)
    protected Order loadPendingOrderWithItems(Long orderId, Long userId) {
        return orderRepository.findByIdAndUserIdWithItems(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
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
        Cart cart = cartRepository.findCartWithItems(userId, CartStatus.CHECKED_OUT)
                .orElseThrow(() -> new NoCheckedOutCartException(userId));

        if (cart.isEmpty()) {
            throw new EmptyCartException();
        }

        // Step 3: Snapshot cart
        Order order = Order.fromCart(cart, currencyCode);
        Order savedOrder = orderRepository.saveAndFlush(order);
        reservationService.attachOrderToReservations(cart.getId(), savedOrder.getId());
        return savedOrder;

    }

    @Transactional
    public Order confirmOrder(Long orderId, Long userId) {
        Order order = orderRepository.findByIdAndUserIdWithItems(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        reservationService.consumeReservations(orderId);
        order.confirm();
        return order;
    }
}
