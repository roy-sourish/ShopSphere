package com.shopsphere.order.service;

import com.shopsphere.cart.domain.Cart;
import com.shopsphere.cart.service.CartService;
import com.shopsphere.inventory.service.InventoryService;
import com.shopsphere.order.domain.Order;
import com.shopsphere.order.domain.OrderStatus;
import com.shopsphere.order.dto.CheckoutResult;
import com.shopsphere.order.exception.EmptyCartException;
import com.shopsphere.order.exception.OrderAccessDeniedException;
import com.shopsphere.order.exception.OrderNotFoundException;
import com.shopsphere.order.repository.OrderRepository;
import com.shopsphere.product.domain.Product;
import com.shopsphere.user.domain.User;
import com.shopsphere.user.exception.UserNotFoundException;
import com.shopsphere.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final InventoryService inventoryService;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository, CartService cartService, InventoryService inventoryService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.cartService = cartService;
        this.inventoryService = inventoryService;
    }

    @Transactional
    public CheckoutResult checkout(Long userId, String idempotencyKey){
        // Step 0: Replay if already exists
        return orderRepository.findByIdempotencyKey(idempotencyKey)
                .map(existing -> new CheckoutResult(existing, true))
                .orElseGet(() -> new CheckoutResult(
                        safeCheckout(userId, idempotencyKey),
                        false
                ));
    }

    private Order safeCheckout(Long userId, String idempotencyKey){
        try{
            return doCheckout(userId, idempotencyKey);
        } catch (DataIntegrityViolationException ex){
            // Another request created the order first -> replay
            return orderRepository.findByIdempotencyKey(idempotencyKey)
                    .orElseThrow(() -> ex);
        }
    }

    private Order doCheckout(Long userId, String idempotencyKey) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Cart cart = cartService.getExistingActiveCart(userId);

        if (cart.isEmpty()) {
            throw new EmptyCartException();
        }
        // Create Order (PENDING)
        Order order = new Order(user, idempotencyKey);
        orderRepository.save(order);

        // Reserve stock + snapshot
        cart.getItems().forEach(cartItem -> {

            Product product = cartItem.getProduct();
            Long productId = product.getId();
            int quantity = cartItem.getQuantity();

            String reservationKey = idempotencyKey + ":" + productId;

            inventoryService.reserveStock(
                    productId,
                    order.getId(),
                    quantity,
                    reservationKey
            );

            order.addItemSnapshot(
                    productId,
                    product.getName(),
                    product.getPrice(),
                    quantity
            );
        });

        cart.checkout();

        orderRepository.flush();
        return order;
    }

    // ======================================================
    // Confirm + Cancel
    // ======================================================

    public Order confirm(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        order.confirm();

        inventoryService.confirmReservation(orderId);

        orderRepository.flush();

        return order;
    }

    public Order cancel(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        order.cancel();

        inventoryService.releaseReservation(orderId);

        orderRepository.flush();

        return order;
    }

    // ======================================================
    // Reads
    // ======================================================
    @Transactional(readOnly = true)
    public Order getOrder(Long orderId, Long userId){
        return getOwnedOrder(orderId, userId);
    }

    @Transactional(readOnly = true)
    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    private Order getOwnedOrder(Long orderId, Long userId) {
        return orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new OrderAccessDeniedException(orderId));
    }

    @Transactional
    public void cancelExpiredOrder(Long orderId){
        // Load order
        Order order = orderRepository.findById(orderId)
                .orElse(null);

        if(order == null){
            return; // already deleted or doesn't exist
        }
        // Only cancel if still pending
        if(order.getStatus() != OrderStatus.PENDING){
            return; // confirmed or already cancelled -> no action
        }

        // Release stock first
        inventoryService.releaseReservation(orderId);

        // Cancel order lifecycle
        order.cancel();

        orderRepository.flush();
    }
}
