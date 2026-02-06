package com.shopsphere.order.repository;

import com.shopsphere.order.domain.Order;
import com.shopsphere.order.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    @Query("""
            SELECT DISTINCT o 
            FROM Order o
            LEFT JOIN FETCH o.items
            WHERE o.user.id = :userId
            AND o.status = :status
            """)
    Optional<Order> findOrderWithItems(Long userId, OrderStatus status);

    @Query("""
            SELECT DISTINCT o 
            FROM Order o
            LEFT JOIN FETCH o.items
            WHERE o.id = :orderId
            AND o.user.id = :userId
            """)
    Optional<Order> findByIdAndUserIdWithItems(Long orderId, Long userId);
}
