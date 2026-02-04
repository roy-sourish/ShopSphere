package com.shopsphere.cart.repository;

import com.shopsphere.cart.domain.Cart;
import com.shopsphere.cart.domain.CartStatus;
import com.shopsphere.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserIdAndStatus(Long userId, CartStatus status);

    @Query("""
                SELECT c FROM Cart c
                LEFT JOIN FETCH c.items i
                LEFT JOIN FETCH i.product
                WHERE c.user.id = :userId
                  AND c.status = :status
            """)
    Optional<Cart> findCartWithItems(Long userId, CartStatus status);

}
